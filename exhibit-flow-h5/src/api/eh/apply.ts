import request from '../request';
import { MOCK_APPLICATIONS, type Application } from '../../mock/applications';

const USE_MOCK = false;

// 后端 status 数字 → 前端枚举
const STATUS_MAP: Record<string, string> = {
  '0': 'draft', '1': 'pending', '2': 'approved',
  '3': 'rejected', '4': 'cancelled', '5': 'rescheduled',
};

// "2026-04-25T09:00:00" 或 "2026-04-25 09:00:00" → "2026-04-25 09:00"
function fmtDt(s: string | null | undefined): string {
  if (!s) return '';
  return s.replace('T', ' ').slice(0, 16);
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
    services: raw.extraServices ? raw.extraServices.split(',').filter(Boolean) : [],
    status: (STATUS_MAP[raw.status] || 'draft') as Application['status'],
    created: fmtDt(raw.createTime),
    opportunityCode: raw.opportunityCode || '',
    approvalNodes: (raw.approvalNodes || []).map((n: any) => ({
      role: n.approver || '',
      name: n.approver || '',
      action: ({ agree: 'approved', reject: 'rejected', pending: 'pending' }[n.action] || 'pending') as any,
      time: fmtDt(n.actionTime),
      comment: n.opinion || '',
    })),
    visitors: (raw.visitors || []).map((v: any) => ({
      name: v.name || '',
      title: v.title || '',
      unit: v.visitorCompany || '',
      isStrategic: v.isKeyCustomer === '1',
      strategicLevel: v.keyCustomerLevel || '',
    })),
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
  if (!raw) return undefined;
  const app = toApplication(raw);
  // 补充审批节点
  try {
    const nodeRes: any = await request.get('/eh/approval/page', { params: { applyId: id, size: 20 } });
    const nodes: any[] = nodeRes?.records ?? (Array.isArray(nodeRes) ? nodeRes : []);
    app.approvalNodes = nodes
      .sort((a, b) => a.nodeLevel - b.nodeLevel)
      .map((n) => ({
        role: n.approver || '',
        name: n.approver || '',
        action: ({ agree: 'approved', reject: 'rejected' }[n.action] || (n.status === 'approved' ? 'approved' : n.status === 'rejected' ? 'rejected' : n.status === 'pending' ? 'pending' : 'waiting')) as any,
        time: fmtDt(n.actionTime),
        comment: n.opinion || '',
      }));
  } catch (_) {}
  return app;
}

export function submitApplication(payload: Record<string, unknown>): Promise<Application> {
  if (USE_MOCK) {
    const app: Application = {
      id: `EH-2026-${String(Math.floor(Math.random() * 9000) + 1000)}`,
      title: String(payload.title),
      unit: String(payload.unit),
      industry: String(payload.industry),
      district: String(payload.district),
      applicant: String(payload.applicant),
      phone: String(payload.phone),
      dept: String(payload.dept),
      startTime: `${payload.startDate} ${payload.startHour}:00`,
      endTime: `${payload.startDate} ${payload.endHour}:00`,
      leader: String(payload.leader),
      visitors: [],
      services: (payload.services as string[]) || [],
      status: 'pending',
      approvalNodes: [
        { role: '部门领导', name: '待分配', action: 'pending', time: null, comment: '' },
        { role: '展厅主管', name: '刘主管', action: 'waiting', time: null, comment: '' },
      ],
      headCount: Number(payload.headCount) || 0,
      agenda: String(payload.agenda || ''),
      created: new Date().toLocaleString('zh-CN'),
      opportunityCode: '',
    };
    MOCK_APPLICATIONS.unshift(app);
    return Promise.resolve(app);
  }
  // 字段名转换为后端格式
  return request.post('/eh/apply', {
    subject: payload.title,
    visitorCompany: payload.unit,
    industry: payload.industry,
    district: payload.district,
    applicant: payload.applicant,
    phone: payload.phone,
    applicantDept: payload.dept,
    startTime: `${payload.startDate} ${payload.startHour}:00:00`,
    endTime: `${payload.startDate} ${payload.endHour}:00:00`,
    topLeaderTitle: payload.leader,
    visitorCount: payload.headCount || 0,
    agenda: payload.agenda,
    extraServices: (payload.services as string[])?.join(',') || '',
    remark: payload.remark,
    status: '1',
  }) as unknown as Promise<Application>;
}

export function cancelApplication(id: string, reason: string) {
  if (USE_MOCK) {
    const t = MOCK_APPLICATIONS.find((a) => a.id === id);
    if (t) t.status = 'cancelled';
    return Promise.resolve({ id, reason });
  }
  return request.put(`/eh/apply/${id}/cancel`, { reason });
}

export function rescheduleApplication(id: string, info: { newDate: string; newSH: string; newEH: string }) {
  if (USE_MOCK) {
    const t = MOCK_APPLICATIONS.find((a) => a.id === id);
    if (t) {
      t.status = 'rescheduled';
      t.startTime = `${info.newDate} ${info.newSH}:00`;
      t.endTime = `${info.newDate} ${info.newEH}:00`;
    }
    return Promise.resolve({ id, ...info });
  }
  // 后端当前无 /eh/apply/{id}/reschedule 路由，改期通过通用更新接口完成
  return request.put('/eh/apply', {
    id,
    startTime: `${info.newDate} ${info.newSH}:00:00`,
    endTime: `${info.newDate} ${info.newEH}:00:00`,
    status: '5', // 5=已改期
  });
}

/**
 * 检测时段是否冲突。
 * 返回 null 表示无冲突；返回字符串为冲突申请的描述（如"华为技术参观 09:00-11:30"）。
 */
export function checkTimeConflict(
  date: string,
  startHour: string,
  endHour: string,
): Promise<string | null> {
  if (USE_MOCK) {
    // 模拟：2026-04-25 上午场与已有申请冲突
    const sh = parseInt(startHour, 10);
    const eh = parseInt(endHour, 10);
    const conflict = date === '2026-04-25' && sh < 12 && eh > 9;
    return new Promise((resolve) =>
      setTimeout(
        () => resolve(conflict ? '华为技术参观（09:00 - 11:30）' : null),
        800,
      ),
    );
  }
  return request.get('/eh/apply/conflict', {
    params: { date, startHour, endHour },
  }) as unknown as Promise<string | null>;
}
