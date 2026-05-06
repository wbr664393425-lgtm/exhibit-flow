
// 通知中心 + 导出预览
const { useState } = React;

// ── Notification Center ───────────────────────────────────────────
const MOCK_NOTIFS = [
  { id:1, type:'approval', icon:'clock', title:'华为技术参观申请待您审批', body:'申请人：李建国 · 天河区', time:'10分钟前', read:false },
  { id:2, type:'approved', icon:'checkCircle', title:'广州市政府采购考察团已获总经理审批通过', body:'申请编号：EH-2026-0415', time:'2小时前', read:false },
  { id:3, type:'reminder', icon:'bell', title:'明日参观提醒', body:'腾讯科技参观 · 2026-04-30 09:30 · 展厅主管已安排', time:'昨天 09:00', read:true },
  { id:4, type:'rejected', icon:'xCircle', title:'中建集团智慧建造申请被驳回', body:'原因：该时段已有安排，请改期', time:'2天前', read:true },
  { id:5, type:'system', icon:'info', title:'系统维护通知', body:'将于本周日凌晨2时进行例行升级维护，预计耗时1小时', time:'3天前', read:true },
];

const NOTIF_COLORS = {
  approval: { c:'#f59e0b', bg:'#fffbeb' },
  approved: { c:T.success,  bg:T.successLight },
  rejected: { c:T.danger,   bg:T.dangerLight },
  reminder: { c:'#1d4ed8',  bg:'#eff6ff' },
  system:   { c:T.text3,    bg:T.bg },
};

const NotificationCenter = ({ onClose, anchor }) => {
  const [notifs, setNotifs] = useState(MOCK_NOTIFS);
  const unread = notifs.filter(n => !n.read).length;
  const markAll = () => setNotifs(ns => ns.map(n => ({...n, read:true})));
  const markOne = id => setNotifs(ns => ns.map(n => n.id===id?{...n,read:true}:n));

  return (
    <div style={{
      position:'fixed', top:54, right:16, width:360, zIndex:800,
      background:T.surface, border:`1px solid ${T.border}`, borderRadius:8,
      boxShadow:'0 12px 40px rgba(17,17,17,0.14)',
      animation:'slideInNotif 0.15s ease',
    }}>
      <style>{`@keyframes slideInNotif{from{opacity:0;transform:translateY(-8px)}to{opacity:1;transform:translateY(0)}}`}</style>

      {/* Header */}
      <div style={{ padding:'14px 16px', borderBottom:`1px solid ${T.border}`, display:'flex', alignItems:'center', justifyContent:'space-between' }}>
        <div style={{ display:'flex', alignItems:'center', gap:8 }}>
          <span style={{ fontSize:14, fontWeight:700, color:T.text1 }}>通知中心</span>
          {unread > 0 && <span style={{ background:T.accent, color:'#fff', fontSize:10, fontWeight:700, padding:'1px 6px', borderRadius:3 }}>{unread}</span>}
        </div>
        <div style={{ display:'flex', gap:6 }}>
          {unread > 0 && <button onClick={markAll} style={{ border:'none', background:'none', cursor:'pointer', fontSize:11, color:T.text3, fontFamily:'inherit', padding:'2px 4px' }}>全部标已读</button>}
          <button onClick={onClose} style={{ border:`1px solid ${T.border}`, background:T.bg, cursor:'pointer', borderRadius:4, padding:'3px 7px', display:'flex', alignItems:'center', color:T.text2 }}>
            <Ic n="x" size={12}/>
          </button>
        </div>
      </div>

      {/* Notification list */}
      <div style={{ maxHeight:400, overflow:'auto' }}>
        {notifs.map((n, i) => {
          const cfg = NOTIF_COLORS[n.type] || NOTIF_COLORS.system;
          return (
            <div key={n.id} onClick={() => markOne(n.id)} style={{
              padding:'12px 16px', borderBottom:i<notifs.length-1?`1px solid ${T.border}`:'none',
              display:'flex', gap:10, cursor:'pointer', transition:'background 0.1s',
              background: n.read ? T.surface : T.bg,
            }}
              onMouseEnter={e=>e.currentTarget.style.background=T.surfaceWarm}
              onMouseLeave={e=>e.currentTarget.style.background=n.read?T.surface:T.bg}>
              <div style={{ width:32, height:32, borderRadius:6, background:cfg.bg, border:`1px solid ${cfg.c}22`, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                <Ic n={n.icon} size={14} color={cfg.c}/>
              </div>
              <div style={{ flex:1, minWidth:0 }}>
                <div style={{ display:'flex', alignItems:'flex-start', gap:6, marginBottom:2 }}>
                  <span style={{ fontSize:12, fontWeight:n.read?500:700, color:T.text1, flex:1, lineHeight:1.4 }}>{n.title}</span>
                  {!n.read && <span style={{ width:6, height:6, borderRadius:'50%', background:T.accent, flexShrink:0, marginTop:3 }}/>}
                </div>
                <div style={{ fontSize:11, color:T.text3, marginBottom:3, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{n.body}</div>
                <MonoLabel style={{ fontSize:9 }}>{n.time}</MonoLabel>
              </div>
            </div>
          );
        })}
      </div>

      <div style={{ padding:'10px 16px', borderTop:`1px solid ${T.border}`, textAlign:'center' }}>
        <button style={{ border:'none', background:'none', cursor:'pointer', fontSize:12, color:T.text3, fontFamily:'inherit' }}>查看全部通知</button>
      </div>
    </div>
  );
};

// ── Export Preview Modal ──────────────────────────────────────────
const EXCEL_COLS = [
  '序号','参观日期','参观单位','客户姓名','职务','是否战客','战客级别',
  '商机编码','参观人数','最高陪同领导','申请部门','申请人','联系电话',
  '现场照片数','实际开始','实际结束','备注',
];

const EXCEL_ROWS = [
  ['1','2026-04-18','广州市政府采购中心','黄主任','采购主任','是','省直管战客','OPP-GZ-2026-0312','11','总经理','公共事业部','陈小红','139****2233','3','14:05','16:20',''],
  ['2','2026-04-25','华为技术有限公司','王总','区域总监','是','市直管战客','','8','分管副总','政企客户部','李建国','138****8801','—','待录入','待录入',''],
  ['3','2026-04-30','腾讯科技（深圳）','刘经理','商务经理','否','','','4','无','政企客户部','李建国','138****8801','—','待录入','待录入',''],
];

const DocxPreview = () => (
  <div style={{ background:'#fff', padding:'40px 48px', maxWidth:640, margin:'0 auto', fontFamily:'Georgia,serif', lineHeight:1.7, boxShadow:'0 2px 16px rgba(0,0,0,0.08)', border:`1px solid ${T.border}` }}>
    <div style={{ textAlign:'center', marginBottom:28 }}>
      <div style={{ fontSize:13, fontWeight:700, letterSpacing:'0.1em', color:T.text1, marginBottom:4 }}></div>
      <div style={{ fontSize:18, fontWeight:700, color:T.text1, letterSpacing:'0.05em', marginBottom:4 }}>展厅使用申请单</div>
      <div style={{ width:100, height:2, background:T.text1, margin:'0 auto 20px' }}/>
    </div>

    {/* Form table */}
    <table style={{ width:'100%', borderCollapse:'collapse', fontSize:12, marginBottom:20 }}>
      <tbody>
        {[
          [['申请编号','EH-2026-0421'],['申请日期','2026年04月22日']],
          [['会议主题','华为技术有限公司参观接待'],['使用时段','2026-04-25 09:00–11:30']],
          [['来访单位','华为技术有限公司'],['所属行业','信息技术']],
          [['预计人数','8 人'],['最高陪同领导','分管副总']],
          [['申请部门','政企客户部'],['申请人','李建国']],
          [['联系电话','138****8801'],['所属区县','天河区']],
        ].map((row, ri) => (
          <tr key={ri}>
            {row.map(([label, val], ci) => (
              <React.Fragment key={ci}>
                <td style={{ border:`1px solid #ccc`, padding:'6px 10px', background:'#f9f8f5', fontWeight:700, width:'15%', whiteSpace:'nowrap' }}>{label}</td>
                <td style={{ border:`1px solid #ccc`, padding:'6px 10px', width:'35%' }}>{val}</td>
              </React.Fragment>
            ))}
          </tr>
        ))}
        <tr>
          <td style={{ border:`1px solid #ccc`, padding:'6px 10px', background:'#f9f8f5', fontWeight:700, whiteSpace:'nowrap' }}>简要议程</td>
          <td colSpan={3} style={{ border:`1px solid #ccc`, padding:'6px 10px' }}>参观5G智慧园区展示区，重点介绍政企解决方案</td>
        </tr>
        <tr>
          <td style={{ border:`1px solid #ccc`, padding:'6px 10px', background:'#f9f8f5', fontWeight:700, whiteSpace:'nowrap' }}>附加服务</td>
          <td colSpan={3} style={{ border:`1px solid #ccc`, padding:'6px 10px' }}>横幅打印、茶水、合影</td>
        </tr>
      </tbody>
    </table>

    {/* Approval section */}
    <div style={{ fontSize:12, fontWeight:700, color:T.text1, marginBottom:8, letterSpacing:'0.05em' }}>审批意见</div>
    <table style={{ width:'100%', borderCollapse:'collapse', fontSize:12 }}>
      <tbody>
        {[['部门领导','陈副总','同意','2026-04-22 10:15'],['展厅主管','刘主管','',''],['分管副总','王副总','','']].map(([role,name,comment,time],i) => (
          <tr key={i}>
            <td style={{ border:`1px solid #ccc`, padding:'8px 10px', background:'#f9f8f5', fontWeight:700, width:'18%' }}>{role}</td>
            <td style={{ border:`1px solid #ccc`, padding:'8px 10px', width:'15%', color:T.text2 }}>{name}</td>
            <td style={{ border:`1px solid #ccc`, padding:'8px 10px', width:'42%', minHeight:32, color:comment?T.text1:T.text3 }}>{comment||'（待审批）'}</td>
            <td style={{ border:`1px solid #ccc`, padding:'8px 10px', width:'25%', color:T.text3, fontSize:11, fontFamily:'monospace' }}>{time}</td>
          </tr>
        ))}
      </tbody>
    </table>

    <div style={{ marginTop:28, display:'flex', justifyContent:'space-between', fontSize:11, color:T.text3 }}>
      <span>系统自动生成 · 展厅访问申请与留存系统</span>
      <span>打印日期：2026-04-23</span>
    </div>
  </div>
);

const ExportPreviewModal = ({ open, onClose }) => {
  const [tab, setTab] = useState('excel');
  return (
    <Modal open={open} title="导出预览" onClose={onClose} width={720}>
      <div style={{ display:'flex', gap:6, marginBottom:16 }}>
        {[['excel','省公司 Excel 报表'],['docx','审批单 Word 文档']].map(([k,l]) => (
          <button key={k} onClick={() => setTab(k)} style={{ border:'none', background:tab===k?T.text1:'transparent', color:tab===k?'#fff':T.text2, padding:'5px 12px', borderRadius:4, cursor:'pointer', fontSize:12, fontWeight:600, display:'flex', alignItems:'center', gap:6, fontFamily:'inherit' }}>
            <Ic n={k==='excel'?'table':'fileText'} size={13} color={tab===k?'#fff':T.text2}/>{l}
          </button>
        ))}
      </div>

      {tab === 'excel' && (
        <div>
          <div style={{ display:'flex', alignItems:'center', gap:8, marginBottom:12 }}>
            <div style={{ fontSize:13, color:T.text2 }}>列结构与原始 <code style={{ background:T.bg, padding:'1px 5px', borderRadius:3, fontSize:11 }}>展厅参观数据表.xlsx Sheet2</code> 完全对齐</div>
            <Btn variant="outline" size="sm" icon="download" onClick={() => { onClose(); }}>下载 Excel</Btn>
          </div>
          <div style={{ overflow:'auto', border:`1px solid ${T.border}`, borderRadius:6 }}>
            <table style={{ width:'100%', borderCollapse:'collapse', fontSize:11, minWidth:900 }}>
              <thead>
                <tr style={{ background:T.text1 }}>
                  {EXCEL_COLS.map(c => (
                    <th key={c} style={{ padding:'7px 10px', color:'#fff', fontWeight:600, whiteSpace:'nowrap', textAlign:'left', borderRight:`1px solid rgba(255,255,255,0.1)`, fontFamily:'monospace', letterSpacing:'0.03em' }}>{c}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {EXCEL_ROWS.map((row, i) => (
                  <tr key={i} style={{ background:i%2===0?T.bg:T.surface }}
                    onMouseEnter={e=>e.currentTarget.style.background=T.accentLight}
                    onMouseLeave={e=>e.currentTarget.style.background=i%2===0?T.bg:T.surface}>
                    {row.map((cell, j) => (
                      <td key={j} style={{ padding:'6px 10px', borderRight:`1px solid ${T.border}`, borderBottom:`1px solid ${T.border}`, color:cell==='待录入'?T.text3:T.text1, whiteSpace:'nowrap' }}>{cell}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div style={{ marginTop:10, fontSize:11, color:T.text3 }}>显示 {EXCEL_ROWS.length} 条 / 共 18 条记录（演示数据）</div>
        </div>
      )}

      {tab === 'docx' && (
        <div>
          <div style={{ display:'flex', alignItems:'center', gap:8, marginBottom:12 }}>
            <div style={{ fontSize:13, color:T.text2 }}>通过 poi-tl 按原模板回填生成存档版申请单</div>
            <Btn variant="outline" size="sm" icon="download" onClick={() => { onClose(); }}>下载 Word</Btn>
          </div>
          <div style={{ overflow:'auto', maxHeight:480, background:'#f5f5f5', padding:20, borderRadius:6 }}>
            <DocxPreview/>
          </div>
        </div>
      )}
    </Modal>
  );
};

Object.assign(window, { NotificationCenter, ExportPreviewModal, MOCK_NOTIFS });
