import request from '../request';
import { MOCK_APPLICATIONS, type Application } from '../../mock/applications';

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
  };
}

function buildPayload(payload: Record<string, unknown>) {
  return {
    title: payload.title,
    unit: payload.unit,
    industry: payload.industry,
    district: payload.district,
    applicant: payload.applicant,
    phone: payload.phone,
    dept: payload.dept,
    startDate: payload.startDate,
    startHour: payload.startHour,
    endHour: payload.endHour,
    leader: payload.leader,
    headCount: Number(payload.headCount || 0),
    agenda: payload.agenda,
    remark: payload.remark,
    services: payload.services || [],
    visitors: payload.visitors || [],
  };
}

export async function fetchMyApplications(): Promise<Application[]> {
  if (USE_MOCK) return MOCK_APPLICATIONS;
  const res: any = await request.get('/eh/apply/page', { params: { size: 50 } });
  const records: any[] = res?.records ?? (Array.isArray(res) ? res : []);
  return records.map(toApplication);
}

export async function fetchApplication(id: string): Promise<Application | undefined> {
  if (USE_MOCK) return MOCK_APPLICATIONS.find((a) => a.id === id);
  const raw: any = await request.get(`/eh/apply/details/${id}`);
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
