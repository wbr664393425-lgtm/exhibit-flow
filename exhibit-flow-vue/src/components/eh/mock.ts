import type { ApplicationStatus } from './tokens';

export interface Visitor {
	name: string;
	title: string;
	unit?: string;
	isStrategic: boolean;
	strategicLevel?: string;
}
export interface ApprovalNodeItem {
	role: string;
	name: string;
	action: 'approved' | 'rejected' | 'pending' | 'waiting';
	time: string | null;
	comment: string;
}
export interface VisitRecord {
	actualStart: string;
	actualEnd: string;
	actualHeadCount: number;
	photos: number;
	returnSigned: boolean;
	returnPerson?: string;
	returnTime?: string;
}
export interface Application {
	id: string;
	title: string;
	unit: string;
	industry: string;
	district: string;
	applicant: string;
	phone: string;
	dept: string;
	startTime: string;
	endTime: string;
	leader: string;
	visitors: Visitor[];
	services: string[];
	status: ApplicationStatus;
	approvalNodes: ApprovalNodeItem[];
	headCount: number;
	agenda: string;
	created: string;
	opportunityCode?: string;
	visitRecord?: VisitRecord;
}

export const MOCK_APPLICATIONS: Application[] = [
	{
		id: 'EH-2026-0421',
		title: '华为技术有限公司参观接待',
		unit: '华为技术有限公司',
		industry: '信息技术',
		district: '天河区',
		applicant: '李建国',
		phone: '138****8801',
		dept: '政企客户部',
		startTime: '2026-04-25 09:00',
		endTime: '2026-04-25 11:30',
		leader: '分管副总',
		visitors: [
			{ name: '王总', title: '区域总监', isStrategic: true, strategicLevel: '市直管战客' },
			{ name: '张工', title: '技术经理', isStrategic: false },
		],
		services: ['横幅打印', '茶水', '合影'],
		status: 'pending',
		approvalNodes: [
			{ role: '部门领导', name: '陈副总', action: 'approved', time: '2026-04-22 10:15', comment: '同意，请做好接待' },
			{ role: '展厅主管', name: '刘主管', action: 'pending', time: null, comment: '' },
			{ role: '分管副总', name: '王副总', action: 'waiting', time: null, comment: '' },
		],
		headCount: 8,
		agenda: '参观5G智慧园区展示区，重点介绍政企解决方案',
		created: '2026-04-22 09:30',
		opportunityCode: '',
	},
	{
		id: 'EH-2026-0415',
		title: '广州市政府采购考察团',
		unit: '广州市政府采购中心',
		industry: '政府机构',
		district: '越秀区',
		applicant: '陈小红',
		phone: '139****2233',
		dept: '公共事业部',
		startTime: '2026-04-18 14:00',
		endTime: '2026-04-18 16:00',
		leader: '总经理',
		visitors: [{ name: '黄主任', title: '采购主任', isStrategic: true, strategicLevel: '省直管战客' }],
		services: ['茶水', '摄影', '车辆接送'],
		status: 'completed',
		approvalNodes: [
			{ role: '部门领导', name: '赵总', action: 'approved', time: '2026-04-16 11:00', comment: '重要客户' },
			{ role: '展厅主管', name: '刘主管', action: 'approved', time: '2026-04-16 14:30', comment: '已安排场地' },
			{ role: '总经理', name: '孙总', action: 'approved', time: '2026-04-17 09:00', comment: '同意' },
		],
		headCount: 12,
		agenda: '政府数字化转型方案演示',
		created: '2026-04-15 16:20',
		opportunityCode: 'OPP-GZ-2026-0312',
		visitRecord: { actualStart: '2026-04-18 14:05', actualEnd: '2026-04-18 16:20', actualHeadCount: 11, photos: 3, returnSigned: true },
	},
	{
		id: 'EH-2026-0408',
		title: '中建集团智慧建造参观',
		unit: '中国建筑集团有限公司',
		industry: '建筑工程',
		district: '海珠区',
		applicant: '林志远',
		phone: '137****5566',
		dept: '政企客户部',
		startTime: '2026-04-12 10:00',
		endTime: '2026-04-12 12:00',
		leader: '分管副总',
		visitors: [
			{ name: '吴副总', title: '副总经理', isStrategic: true, strategicLevel: '市直管战客' },
			{ name: '周总监', title: '信息化总监', isStrategic: true, strategicLevel: '区县直管战客' },
		],
		services: ['横幅打印', '茶水', '合影', '摄影'],
		status: 'rejected',
		approvalNodes: [
			{ role: '部门领导', name: '陈副总', action: 'approved', time: '2026-04-09 09:00', comment: '' },
			{ role: '展厅主管', name: '刘主管', action: 'rejected', time: '2026-04-09 15:00', comment: '该时段已有安排，请改期' },
		],
		headCount: 6,
		agenda: '建筑行业物联网、智慧工地解决方案演示',
		created: '2026-04-08 14:00',
		opportunityCode: '',
	},
	{
		id: 'EH-2026-0430',
		title: '深圳腾讯科技参观接待',
		unit: '腾讯科技（深圳）有限公司',
		industry: '互联网',
		district: '番禺区',
		applicant: '李建国',
		phone: '138****8801',
		dept: '政企客户部',
		startTime: '2026-04-30 09:30',
		endTime: '2026-04-30 11:00',
		leader: '无',
		visitors: [{ name: '刘经理', title: '商务经理', isStrategic: false }],
		services: ['茶水'],
		status: 'approved',
		approvalNodes: [
			{ role: '部门领导', name: '陈副总', action: 'approved', time: '2026-04-23 10:00', comment: '同意' },
			{ role: '展厅主管', name: '刘主管', action: 'approved', time: '2026-04-23 11:30', comment: '已安排' },
		],
		headCount: 4,
		agenda: '云计算、CDN产品介绍',
		created: '2026-04-22 16:00',
		opportunityCode: '',
	},
];

export const MOCK_CALENDAR = [
	{ id: 'EH-2026-0421', title: '华为技术参观', start: '2026-04-25', startTime: '09:00', end: '11:30', status: 'pending' as ApplicationStatus },
	{ id: 'EH-2026-0430', title: '腾讯科技参观', start: '2026-04-30', startTime: '09:30', end: '11:00', status: 'approved' as ApplicationStatus },
	{ id: 'CAL-001', title: '中国移动考察', start: '2026-04-25', startTime: '14:00', end: '16:00', status: 'approved' as ApplicationStatus },
	{ id: 'CAL-002', title: '省发改委调研', start: '2026-04-28', startTime: '10:00', end: '12:00', status: 'approved' as ApplicationStatus },
	{ id: 'CAL-003', title: '中石化集团参观', start: '2026-04-29', startTime: '14:30', end: '16:30', status: 'approved' as ApplicationStatus },
];

export const SERVICES = ['横幅打印', '茶水', '合影', '摄影', '车辆接送'];
export const INDUSTRIES = ['信息技术', '政府机构', '金融保险', '建筑工程', '制造业', '能源电力', '互联网', '教育医疗', '商业零售', '其他'];
export const DISTRICTS = ['天河区', '越秀区', '荔湾区', '海珠区', '白云区', '黄埔区', '番禺区', '花都区', '增城区', '从化区', '南沙区'];
export const LEADERS = ['无', '部门副总', '分管副总', '总经理'];
export const DEPTS = ['政企客户部', '公共事业部', '家庭客户部', '市场部', '综合部'];
