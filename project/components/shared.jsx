
// Design tokens (Intercom-inspired warm system) + SVG icons + base components

const T = {
  bg:           '#faf9f6',   // warm cream canvas
  surface:      '#ffffff',
  surfaceWarm:  '#f5f4f0',   // slightly toasted
  border:       '#dedbd6',   // warm oat
  borderDark:   '#c8c4be',
  text1:        '#111111',   // off-black
  text2:        '#626260',   // black-60
  text3:        '#9c9fa5',   // tertiary
  accent:       '#ff5600',   // Fin Orange
  accentLight:  '#fff2eb',
  accentBorder: '#ffcfb3',
  success:      '#2c6415',
  successLight: '#f0f9eb',
  warning:      '#92400e',
  warningLight: '#fffbeb',
  danger:       '#c41c1c',
  dangerLight:  '#fef2f2',
  sidebar:      '#111111',
  sidebarSub:   '#1c1c1a',
};

const STATUS_MAP = {
  draft:       { label:'草稿',   color:'#9c9fa5', bg:'#f5f4f0', dot:'#9c9fa5' },
  pending:     { label:'审批中', color:'#92400e', bg:'#fffbeb', dot:'#f79009' },
  approved:    { label:'已批准', color:'#2c6415', bg:'#f0f9eb', dot:'#2c6415' },
  rejected:    { label:'已驳回', color:'#c41c1c', bg:'#fef2f2', dot:'#c41c1c' },
  cancelled:   { label:'已取消', color:'#9c9fa5', bg:'#f5f4f0', dot:'#9c9fa5' },
  completed:   { label:'已完成', color:'#1d4ed8', bg:'#eff6ff', dot:'#1d4ed8' },
  rescheduled: { label:'已改期', color:'#6b21a8', bg:'#faf5ff', dot:'#7e22ce' },
};

// ── SVG Icons ─────────────────────────────────────────────────────
const ICONS = {
  building:    'M3 21h18M5 21V7l7-4 7 4v14M9 21v-6h6v6M9 9h2M13 9h2M9 13h2M13 13h2',
  filePlus:    'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M12 18v-6M9 15h6',
  list:        'M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01',
  checkCircle: 'M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4 12 14.01l-3-3',
  clock:       'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM12 6v6l4 2',
  calendar:    'M3 4h18a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zM16 2v4M8 2v4M2 10h20',
  camera:      'M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2zM12 17a4 4 0 1 0 0-8 4 4 0 0 0 0 8z',
  barChart:    'M18 20V10M12 20V4M6 20v-6',
  user:        'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z',
  users:       'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
  bell:        'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0',
  chevronRight:'M9 18l6-6-6-6',
  chevronDown: 'M6 9l6 6 6-6',
  chevronLeft: 'M15 18l-6-6 6-6',
  x:           'M18 6 6 18M6 6l12 12',
  plus:        'M12 5v14M5 12h14',
  check:       'M20 6 9 17l-5-5',
  alertTri:    'M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0zM12 9v4M12 17h.01',
  info:        'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM12 16v-4M12 8h.01',
  star:        'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z',
  briefcase:   'M20 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2zM16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2',
  download:    'M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3',
  upload:      'M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12',
  search:      'M21 21l-4.35-4.35M17 11A6 6 0 1 1 5 11a6 6 0 0 1 12 0z',
  settings:    'M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z',
  arrowLeft:   'M19 12H5M12 19l-7-7 7-7',
  eye:         'M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8zM12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z',
  edit:        'M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z',
  rotate:      'M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15',
  phone:       'M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z',
  clipboard:   'M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2M9 2h6a1 1 0 0 1 1 1v2a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1z',
  send:        'M22 2 11 13M22 2 15 22 11 13 2 9l20-7z',
  smartphone:  'M17 2H7a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2zM12 18h.01',
  monitor:     'M21 2H3a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1zM8 21h8M12 17v4',
  trendUp:     'M23 6l-9.5 9.5-5-5L1 18M17 6h6v6',
  table:       'M3 3h18v4H3zM3 11h18v2H3zM3 17h18v4H3z',
  fileText:    'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8',
  xCircle:     'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM15 9l-6 6M9 9l6 6',
  checkSquare: 'M9 11l3 3L22 4M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11',
  mapPin:      'M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0zM12 7a3 3 0 1 0 0 6 3 3 0 0 0 0-6z',
  layers:      'M12 2 2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5',
  home:        'M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM9 22V12h6v10',
  filter:      'M22 3H2l8 9.46V19l4 2v-8.54L22 3',
};

const Ic = ({ n, size=16, color='currentColor', sw=1.8 }) => {
  const d = ICONS[n];
  if (!d) return <span style={{width:size,height:size,display:'inline-block',flexShrink:0}}/>;
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
      stroke={color} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round"
      style={{display:'block',flexShrink:0}}>
      {d.split('M').filter(Boolean).map((p,i)=><path key={i} d={'M'+p}/>)}
    </svg>
  );
};

// ── Mock Data ─────────────────────────────────────────────────────
const MOCK_APPLICATIONS = [
  { id:'EH-2026-0421', title:'华为技术有限公司参观接待', unit:'华为技术有限公司', industry:'信息技术', district:'天河区', applicant:'李建国', phone:'138****8801', dept:'政企客户部', startTime:'2026-04-25 09:00', endTime:'2026-04-25 11:30', leader:'分管副总', visitors:[{ name:'王总', title:'区域总监', isStrategic:true, strategicLevel:'市直管战客' },{ name:'张工', title:'技术经理', isStrategic:false }], services:['横幅打印','茶水','合影'], status:'pending', approvalNodes:[{ role:'部门领导', name:'陈副总', action:'approved', time:'2026-04-22 10:15', comment:'同意，请做好接待' },{ role:'展厅主管', name:'刘主管', action:'pending', time:null, comment:'' },{ role:'分管副总', name:'王副总', action:'waiting', time:null, comment:'' }], headCount:8, agenda:'参观5G智慧园区展示区，重点介绍政企解决方案', created:'2026-04-22 09:30', opportunityCode:'' },
  { id:'EH-2026-0415', title:'广州市政府采购考察团', unit:'广州市政府采购中心', industry:'政府机构', district:'越秀区', applicant:'陈小红', phone:'139****2233', dept:'公共事业部', startTime:'2026-04-18 14:00', endTime:'2026-04-18 16:00', leader:'总经理', visitors:[{ name:'黄主任', title:'采购主任', isStrategic:true, strategicLevel:'省直管战客' }], services:['茶水','摄影','车辆接送'], status:'completed', approvalNodes:[{ role:'部门领导', name:'赵总', action:'approved', time:'2026-04-16 11:00', comment:'重要客户' },{ role:'展厅主管', name:'刘主管', action:'approved', time:'2026-04-16 14:30', comment:'已安排场地' },{ role:'总经理', name:'孙总', action:'approved', time:'2026-04-17 09:00', comment:'同意' }], headCount:12, agenda:'政府数字化转型方案演示', created:'2026-04-15 16:20', opportunityCode:'OPP-GZ-2026-0312', visitRecord:{ actualStart:'2026-04-18 14:05', actualEnd:'2026-04-18 16:20', actualHeadCount:11, photos:3, returnSigned:true } },
  { id:'EH-2026-0408', title:'中建集团智慧建造参观', unit:'中国建筑集团有限公司', industry:'建筑工程', district:'海珠区', applicant:'林志远', phone:'137****5566', dept:'政企客户部', startTime:'2026-04-12 10:00', endTime:'2026-04-12 12:00', leader:'分管副总', visitors:[{ name:'吴副总', title:'副总经理', isStrategic:true, strategicLevel:'市直管战客' },{ name:'周总监', title:'信息化总监', isStrategic:true, strategicLevel:'区县直管战客' }], services:['横幅打印','茶水','合影','摄影'], status:'rejected', approvalNodes:[{ role:'部门领导', name:'陈副总', action:'approved', time:'2026-04-09 09:00', comment:'' },{ role:'展厅主管', name:'刘主管', action:'rejected', time:'2026-04-09 15:00', comment:'该时段已有安排，请改期' }], headCount:6, agenda:'建筑行业物联网、智慧工地解决方案演示', created:'2026-04-08 14:00', opportunityCode:'' },
  { id:'EH-2026-0430', title:'深圳腾讯科技参观接待', unit:'腾讯科技（深圳）有限公司', industry:'互联网', district:'番禺区', applicant:'李建国', phone:'138****8801', dept:'政企客户部', startTime:'2026-04-30 09:30', endTime:'2026-04-30 11:00', leader:'无', visitors:[{ name:'刘经理', title:'商务经理', isStrategic:false }], services:['茶水'], status:'approved', approvalNodes:[{ role:'部门领导', name:'陈副总', action:'approved', time:'2026-04-23 10:00', comment:'同意' },{ role:'展厅主管', name:'刘主管', action:'approved', time:'2026-04-23 11:30', comment:'已安排' }], headCount:4, agenda:'云计算、CDN产品介绍', created:'2026-04-22 16:00', opportunityCode:'' },
];

const MOCK_CALENDAR = [
  { id:'EH-2026-0421', title:'华为技术参观', start:'2026-04-25', startTime:'09:00', end:'11:30', status:'pending' },
  { id:'EH-2026-0430', title:'腾讯科技参观', start:'2026-04-30', startTime:'09:30', end:'11:00', status:'approved' },
  { id:'CAL-001', title:'中国移动考察', start:'2026-04-25', startTime:'14:00', end:'16:00', status:'approved' },
  { id:'CAL-002', title:'省发改委调研', start:'2026-04-28', startTime:'10:00', end:'12:00', status:'approved' },
  { id:'CAL-003', title:'中石化集团参观', start:'2026-04-29', startTime:'14:30', end:'16:30', status:'approved' },
];

// ── Base Components ───────────────────────────────────────────────
const MonoLabel = ({ children, style={} }) => (
  <span style={{ fontFamily:"'JetBrains Mono',monospace", fontSize:10, fontWeight:500, textTransform:'uppercase', letterSpacing:'0.9px', color:T.text3, ...style }}>{children}</span>
);

const Badge = ({ status }) => {
  const s = STATUS_MAP[status] || STATUS_MAP.pending;
  return (
    <span style={{ display:'inline-flex', alignItems:'center', gap:5, padding:'3px 8px', borderRadius:4, fontSize:11, fontWeight:600, color:s.color, background:s.bg, border:`1px solid ${s.dot}33`, letterSpacing:'0.02em', whiteSpace:'nowrap' }}>
      <span style={{ width:5, height:5, borderRadius:'50%', background:s.dot, display:'inline-block', flexShrink:0 }}/>
      {s.label}
    </span>
  );
};

const Card = ({ children, style={}, onClick }) => {
  const [hov, setHov] = React.useState(false);
  return (
    <div onClick={onClick} onMouseEnter={()=>onClick&&setHov(true)} onMouseLeave={()=>setHov(false)}
      style={{ background:T.surface, borderRadius:8, border:`1px solid ${hov?T.borderDark:T.border}`, transition:'border-color 0.15s', cursor:onClick?'pointer':'default', ...style }}>
      {children}
    </div>
  );
};

const Btn = ({ children, variant='primary', size='md', onClick, disabled, icon, style={} }) => {
  const [hov, setHov] = React.useState(false);
  const [act, setAct] = React.useState(false);
  const V = {
    primary:   { bg:T.text1,   color:'#fff',      hBg:'#2d2d2b', border:'none' },
    orange:    { bg:T.accent,  color:'#fff',       hBg:'#e64d00', border:'none' },
    secondary: { bg:T.bg,      color:T.text1,      hBg:T.surfaceWarm, border:`1px solid ${T.border}` },
    ghost:     { bg:'transparent', color:T.text2,  hBg:T.bg,     border:`1px solid ${T.border}` },
    danger:    { bg:T.danger,  color:'#fff',       hBg:'#991b1b', border:'none' },
    success:   { bg:T.success, color:'#fff',       hBg:'#166534', border:'none' },
    outline:   { bg:'transparent', color:T.text1,  hBg:T.bg,     border:`1px solid ${T.text1}` },
  };
  const S = { sm:{p:'4px 10px',fs:11}, md:{p:'7px 14px',fs:13}, lg:{p:'10px 20px',fs:14} };
  const v=V[variant]||V.primary; const s=S[size]||S.md;
  return (
    <button onClick={onClick} disabled={disabled}
      onMouseEnter={()=>!disabled&&setHov(true)} onMouseLeave={()=>{setHov(false);setAct(false);}}
      onMouseDown={()=>!disabled&&setAct(true)} onMouseUp={()=>setAct(false)}
      style={{
        background:hov?v.hBg:v.bg, color:v.color, border:v.border||'none',
        padding:s.p, fontSize:s.fs, borderRadius:4, fontWeight:600,
        cursor:disabled?'not-allowed':'pointer', opacity:disabled?0.45:1,
        transition:'background 0.1s, transform 0.1s',
        transform:act?'scale(0.95)':hov?'scale(1.04)':'scale(1)',
        display:'inline-flex', alignItems:'center', gap:6,
        fontFamily:'inherit', whiteSpace:'nowrap', letterSpacing:'0.01em', ...style,
      }}>
      {icon && <Ic n={icon} size={parseInt(s.fs)+1} color={v.color}/>}
      {children}
    </button>
  );
};

const FancyInput = ({ label, value, onChange, placeholder, type='text', required, disabled, hint, children }) => (
  <div style={{ display:'flex', flexDirection:'column', gap:4 }}>
    {label && <MonoLabel>{required&&<span style={{color:T.accent,marginRight:3}}>✱</span>}{label}</MonoLabel>}
    {children || (
      <input type={type} value={value} onChange={e=>onChange&&onChange(e.target.value)}
        placeholder={placeholder} disabled={disabled}
        style={{ border:`1px solid ${T.border}`, borderRadius:4, padding:'8px 10px', fontSize:13, color:T.text1, outline:'none', background:disabled?T.bg:T.surface, width:'100%', boxSizing:'border-box', transition:'border-color 0.15s', fontFamily:'inherit' }}
        onFocus={e=>e.target.style.borderColor=T.text1}
        onBlur={e=>e.target.style.borderColor=T.border}/>
    )}
    {hint && <span style={{ fontSize:11, color:T.text3 }}>{hint}</span>}
  </div>
);

const FancySelect = ({ label, value, onChange, options, required, disabled }) => (
  <div style={{ display:'flex', flexDirection:'column', gap:4 }}>
    {label && <MonoLabel>{required&&<span style={{color:T.accent,marginRight:3}}>✱</span>}{label}</MonoLabel>}
    <div style={{ position:'relative' }}>
      <select value={value} onChange={e=>onChange&&onChange(e.target.value)} disabled={disabled}
        style={{ border:`1px solid ${T.border}`, borderRadius:4, padding:'8px 10px', fontSize:13, color:T.text1, outline:'none', background:disabled?T.bg:T.surface, width:'100%', boxSizing:'border-box', cursor:'pointer', appearance:'none', fontFamily:'inherit' }}>
        {options.map(o=><option key={o.value||o} value={o.value||o}>{o.label||o}</option>)}
      </select>
      <span style={{ position:'absolute', right:8, top:'50%', transform:'translateY(-50%)', pointerEvents:'none', color:T.text3 }}><Ic n="chevronDown" size={13}/></span>
    </div>
  </div>
);

const Modal = ({ open, title, onClose, children, width=520 }) => {
  if (!open) return null;
  return (
    <div style={{ position:'fixed', inset:0, background:'rgba(17,17,17,0.55)', zIndex:1000, display:'flex', alignItems:'center', justifyContent:'center', padding:20, backdropFilter:'blur(3px)' }}
      onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div style={{ background:T.surface, borderRadius:8, width:Math.min(width,'90vw'), maxHeight:'90vh', overflow:'auto', border:`1px solid ${T.border}`, boxShadow:'0 24px 60px rgba(17,17,17,0.22)' }}>
        <div style={{ padding:'16px 20px', borderBottom:`1px solid ${T.border}`, display:'flex', alignItems:'center', justifyContent:'space-between', position:'sticky', top:0, background:T.surface, zIndex:1 }}>
          <span style={{ fontSize:15, fontWeight:700, color:T.text1 }}>{title}</span>
          <button onClick={onClose} style={{ border:`1px solid ${T.border}`, background:T.bg, cursor:'pointer', borderRadius:4, padding:'4px 8px', display:'flex', color:T.text2, transition:'background 0.1s' }}
            onMouseEnter={e=>e.currentTarget.style.background=T.surfaceWarm}
            onMouseLeave={e=>e.currentTarget.style.background=T.bg}>
            <Ic n="x" size={14}/>
          </button>
        </div>
        <div style={{ padding:'20px' }}>{children}</div>
      </div>
    </div>
  );
};

const ApprovalTimeline = ({ nodes }) => {
  const ic = { approved:'checkCircle', rejected:'xCircle', pending:'clock', waiting:'clock' };
  const cc = { approved:T.success, rejected:T.danger, pending:'#f59e0b', waiting:T.border };
  return (
    <div style={{ display:'flex', flexDirection:'column' }}>
      {nodes.map((n,i)=>{
        const isLast=i===nodes.length-1;
        const c=cc[n.action]||T.border;
        return (
          <div key={i} style={{ display:'flex', gap:12 }}>
            <div style={{ display:'flex', flexDirection:'column', alignItems:'center' }}>
              <div style={{ width:28, height:28, borderRadius:'50%', background:n.action==='waiting'?T.bg:c+'18', border:`1.5px solid ${c}`, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                <Ic n={ic[n.action]||'clock'} size={13} color={n.action==='waiting'?T.text3:c}/>
              </div>
              {!isLast&&<div style={{ width:1, flex:1, background:T.border, margin:'3px 0', minHeight:18 }}/>}
            </div>
            <div style={{ paddingBottom:isLast?0:18, flex:1, paddingTop:3 }}>
              <div style={{ display:'flex', alignItems:'center', gap:8, flexWrap:'wrap' }}>
                <span style={{ fontSize:13, fontWeight:600, color:T.text1 }}>{n.role}</span>
                <span style={{ fontSize:12, color:T.text2 }}>{n.name}</span>
                {n.time&&<span style={{ fontSize:11, color:T.text3, marginLeft:'auto' }}>{n.time}</span>}
              </div>
              {n.comment&&<p style={{ margin:'4px 0 0', fontSize:12, color:T.text2, background:T.bg, padding:'5px 9px', borderRadius:4, border:`1px solid ${T.border}` }}>{n.comment}</p>}
              {n.action==='waiting'&&<p style={{ margin:'3px 0 0', fontSize:11, color:T.text3 }}>等待上一节点完成后触发</p>}
            </div>
          </div>
        );
      })}
    </div>
  );
};

const Toast = ({ msg, type='success', onClose }) => {
  if (!msg) return null;
  const cfg = { success:{ic:'checkCircle',c:T.success}, error:{ic:'alertTri',c:T.danger}, info:{ic:'info',c:'#1d4ed8'}, warning:{ic:'alertTri',c:T.warning} };
  const cf = cfg[type]||cfg.info;
  return (
    <div style={{ position:'fixed', top:20, right:20, zIndex:2000, background:T.surface, borderRadius:6, padding:'12px 16px', boxShadow:'0 8px 32px rgba(17,17,17,0.14)', display:'flex', alignItems:'center', gap:10, borderLeft:`3px solid ${cf.c}`, minWidth:240, maxWidth:360, border:`1px solid ${T.border}`, borderLeft:`3px solid ${cf.c}` }}>
      <Ic n={cf.ic} size={16} color={cf.c}/>
      <span style={{ fontSize:13, color:T.text1, flex:1 }}>{msg}</span>
      <button onClick={onClose} style={{ border:'none', background:'none', cursor:'pointer', color:T.text3, display:'flex' }}><Ic n="x" size={13}/></button>
    </div>
  );
};

// SVG Line Chart
const SVGLineChart = ({ data, h=160 }) => {
  const W=400; const PL=36; const PR=16; const PT=12; const PB=28;
  const cW=W-PL-PR; const cH=h-PT-PB;
  const allV=data.flatMap(d=>[d.v1,d.v2]).filter(v=>v>0);
  const maxV=Math.max(...allV,1);
  const px=i=>PL+(i/(data.length-1))*cW;
  const py=v=>PT+cH-(v/maxV)*cH;
  const path1=data.filter(d=>d.v1>0).map((d,_,a)=>{ const i=data.indexOf(d); return `${i===0?'M':'L'}${px(i).toFixed(1)},${py(d.v1).toFixed(1)}`; }).join(' ');
  const path2=data.filter(d=>d.v2>0).map((d,_,a)=>{ const i=data.indexOf(d); return `${i===0?'M':'L'}${px(i).toFixed(1)},${py(d.v2).toFixed(1)}`; }).join(' ');
  const area1=`${path1} L${px(data.filter(d=>d.v1>0).length-1).toFixed(1)},${(PT+cH).toFixed(1)} L${px(0).toFixed(1)},${(PT+cH).toFixed(1)} Z`;
  return (
    <svg viewBox={`0 0 ${W} ${h}`} style={{width:'100%',height:h}} preserveAspectRatio="xMidYMid meet">
      <defs>
        <linearGradient id="g1" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={T.accent} stopOpacity="0.15"/>
          <stop offset="100%" stopColor={T.accent} stopOpacity="0"/>
        </linearGradient>
      </defs>
      {[0,0.25,0.5,0.75,1].map((r,i)=>(
        <line key={i} x1={PL} y1={(PT+cH*(1-r)).toFixed(1)} x2={PL+cW} y2={(PT+cH*(1-r)).toFixed(1)} stroke={T.border} strokeWidth="0.8"/>
      ))}
      {data.map((d,i)=>(
        <line key={i} x1={px(i).toFixed(1)} y1={PT} x2={px(i).toFixed(1)} y2={PT+cH} stroke={T.border} strokeWidth="0.5" strokeDasharray="3,3"/>
      ))}
      {path1&&<path d={area1} fill="url(#g1)"/>}
      {path2&&<path d={path2} fill="none" stroke={T.border} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" strokeDasharray="4,3"/>}
      {path1&&<path d={path1} fill="none" stroke={T.accent} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>}
      {data.map((d,i)=>d.v1>0&&(
        <circle key={i} cx={px(i).toFixed(1)} cy={py(d.v1).toFixed(1)} r="3" fill={T.accent} stroke={T.surface} strokeWidth="1.5"/>
      ))}
      {data.map((d,i)=>(
        <text key={i} x={px(i).toFixed(1)} y={h-6} textAnchor="middle" fontSize="9" fill={T.text3} fontFamily="inherit">{d.label}</text>
      ))}
      {[Math.round(maxV*0.5),maxV].map((v,i)=>(
        <text key={i} x={PL-4} y={(py(v)+3).toFixed(1)} textAnchor="end" fontSize="9" fill={T.text3} fontFamily="inherit">{v}</text>
      ))}
    </svg>
  );
};

Object.assign(window, {
  T, STATUS_MAP, MOCK_APPLICATIONS, MOCK_CALENDAR,
  Ic, MonoLabel, Badge, Card, Btn, FancyInput, FancySelect, Modal,
  ApprovalTimeline, Toast, SVGLineChart,
});
