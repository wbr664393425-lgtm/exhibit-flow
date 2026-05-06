import request from '../request';
import { MOCK_APPLICATIONS, type Application, type ApplyHistoryEvent } from '../../mock/applications';

const USE_MOCK = false;

const STATUS_MAP: Record<string, Application['status']> = {
  '0': 'draft',
  '1': 'pending',
  '2': 'approved',
  '3': 'rejected',
  '4': 'cancelled',
  '5': 'rescheduled',
};

function fmtDt(s: string | null | undefined): string {
  if (!s) return '';
  return s.replace('T', ' ').slice(0, 16);
}

function mapNodeAction(raw: any) {
  if (raw?.status === 'approved' || raw?.action === 'agree') return 'approved';
  if (raw?.status === 'rejected' || raw?.action === 'reject') return 'rejected';
  if (raw?.status === 'pending') return 'pending';
  return 'waiting';
}

function toApplication(raw: any): Application {
  return {
    id: String(raw.id),
    title: raw.subject || '',
    meetingNature: raw.meetingNature || 'external',
    unit: raw.visitorCompany || '',
    industry: raw.industry || '',
    district: raw.district || '',
    applicant: raw.applicant || '',
    phone: raw.phone || '',
    dept: raw.applicantDept || '',
    startTime: fmtDt(raw.startTime),
    endTime: fmtDt(raw.endTime),
    leader: raw.topLeaderTitle || '无',
    headCount: raw.visitorCount || 0,
    customerCount: raw.customerCount ?? raw.visitorCount ?? 0,
    internalCount: raw.internalCount ?? 0,
    agenda: raw.agenda || '',
    services: raw.extraServices ? String(raw.extraServices).split(',').filter(Boolean) : [],
    status: STATUS_MAP[raw.status] || 'draft',
    created: fmtDt(raw.createTime),
    opportunityCode: raw.opportunityCode || '',
    approvalNodes: (raw.approvalNodes || []).map((n: any) => ({
      role: n.approver || '',
      name: n.approver || '',
      action: mapNodeAction(n),
      time: fmtDt(n.actionTime),
      comment: n.opinion || '',
    })),
    visitors: (raw.visitors || []).map((v: any) => ({
      name: v.name || '',
      title: v.title || '',
      unit: v.unit || v.visitorCompany || '',
      isStrategic: v.isKeyCustomer === '1',
      strategicLevel: v.keyCustomerLevel || '',
    })),
    history: (raw.history || []).map((h: any): ApplyHistoryEvent => ({
      eventType: h.eventType || '',
      eventDesc: h.eventDesc || '',
      operator: h.operator || '',
      remark: h.remark || null,
      eventTime: fmtDt(h.eventTime),
    })),
  };
}

function buildPayload(payload: Record<string, unknown>) {
  return {
    title: payload.title,
    meetingNature: payload.meetingNature || 'external',
    unit: payload.unit,
    industry: payload.industry,
    district: payload.district,
    applicant: payload.applicant,
    phone: payload.phone,
    dept: payload.dept,
    startDate: payload.startDate,
    meetingTime: payload.meetingTime || payload.startHour,
    startHour: payload.meetingTime || payload.startHour,
    endHour: payload.endHour || payload.meetingTime || payload.startHour,
    leader: payload.leader,
    customerCount: Number(payload.customerCount || 0),
    internalCount: Number(payload.internalCount || 0),
    headCount: Number(payload.customerCount || 0) + Number(payload.internalCount || 0),
    agenda: payload.agenda,
    remark: payload.remark,
    services: payload.services || [],
    visitors: payload.visitors || [],
  };
}

export async function fetchMyApplications(): Promise<Application[]> {
  if (USE_MOCK) return MOCK_APPLICATIONS;
  const res: any = await request.get('/eh/apply/my/page', { params: { current: 1, size: 50 } });
  const records: any[] = res?.records ?? (Array.isArray(res) ? res : []);
  return records.map(toApplication);
}

export interface MyApplicationsPageResult {
  records: Application[];
  total: number;
  current: number;
  size: number;
}

export async function fetchMyApplicationsPage(current = 1, size = 10): Promise<MyApplicationsPageResult> {
  if (USE_MOCK) {
    const start = (current - 1) * size;
    const end = start + size;
    return {
      records: MOCK_APPLICATIONS.slice(start, end),
      total: MOCK_APPLICATIONS.length,
      current,
      size,
    };
  }
  const res: any = await request.get('/eh/apply/my/page', { params: { current, size } });
  const records: any[] = res?.records ?? (Array.isArray(res) ? res : []);
  return {
    records: records.map(toApplication),
    total: Number(res?.total ?? records.length ?? 0),
    current: Number(res?.current ?? current),
    size: Number(res?.size ?? size),
  };
}

export async function fetchApplication(id: string): Promise<Application | undefined> {
  if (USE_MOCK) return MOCK_APPLICATIONS.find((a) => a.id === id);
  const raw: any = await request.get(`/eh/apply/my/details/${id}`);
  return raw ? toApplication(raw) : undefined;
}

export async function saveDraftApplication(payload: Record<string, unknown>): Promise<void> {
  if (USE_MOCK) {
    return;
  }
  await request.post('/eh/apply/draft', buildPayload(payload));
}

export async function submitApplication(payload: Record<string, unknown>): Promise<void> {
  if (USE_MOCK) {
    return;
  }
  await request.post('/eh/apply/submit', buildPayload(payload));
}

export function cancelApplication(id: string, reason: string) {
  if (USE_MOCK) {
    const target = MOCK_APPLICATIONS.find((a) => a.id === id);
    if (target) target.status = 'cancelled';
    return Promise.resolve({ id, reason });
  }
  return request.put(`/eh/apply/${id}/cancel`, { reason });
}

export function rescheduleApplication(id: string, info: { newDate: string; newSH: string; newEH: string; reason?: string }) {
  if (USE_MOCK) {
    const target = MOCK_APPLICATIONS.find((a) => a.id === id);
    if (target) {
      target.status = 'rescheduled';
      target.startTime = `${info.newDate} ${info.newSH}:00`;
      target.endTime = `${info.newDate} ${info.newEH}:00`;
    }
    return Promise.resolve({ id, ...info });
  }
  return request.put(`/eh/apply/${id}/reschedule`, info);
}

export function resubmitApplication(id: string, payload: Record<string, unknown>): Promise<void> {
  if (USE_MOCK) return Promise.resolve();
  return request.put(`/eh/apply/${id}/submit`, buildPayload(payload)) as unknown as Promise<void>;
}

export function checkTimeConflict(date: string, startHour: string, endHour: string): Promise<string | null> {
  if (USE_MOCK) {
    const sh = parseInt(startHour, 10);
    const eh = parseInt(endHour, 10);
    const conflict = date === '2026-04-25' && sh < 12 && eh > 9;
    return new Promise((resolve) => setTimeout(() => resolve(conflict ? '华为技术参观（09:00 - 11:30）' : null), 800));
  }
  return request.get('/eh/apply/conflict', {
    params: { date, startHour, endHour },
  }) as unknown as Promise<string | null>;
}

export async function fetchDaySchedules(date: string): Promise<Application[]> {
  if (USE_MOCK) {
    return MOCK_APPLICATIONS.filter((item) => item.startTime.startsWith(date));
  }
  const res: any = await request.get('/eh/apply/calendar', {
    params: { start: date, end: date },
  });
  return (Array.isArray(res) ? res : []).map(toApplication);
}
