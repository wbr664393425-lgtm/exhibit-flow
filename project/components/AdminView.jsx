
// AdminView — 审批+日历+留存（归还签收）+增强看板
const { useState } = React;

// ============ APPROVAL VIEW ============
const ApprovalView = ({ notify }) => {
  const [apps, setApps] = useState(MOCK_APPLICATIONS.filter(a => a.status === 'pending'));
  const [sel, setSel] = useState(null);
  const [modal, setModal] = useState(null);
  const [comment, setComment] = useState('');

  const doAction = type => {
    setApps(p => p.filter(a => a.id !== sel.id));
    setSel(null); setModal(null); setComment('');
    notify(type === 'approve' ? '审批通过，流程已流转' : '已驳回，申请人将收到通知', type === 'approve' ? 'success' : 'error');
  };

  return (
    <div style={{ display:'flex', gap:18, height:'calc(100vh - 112px)' }}>
      {/* Left */}
      <div style={{ width:300, flexShrink:0, display:'flex', flexDirection:'column', gap:0 }}>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:12 }}>
          <h2 style={{ margin:0, fontSize:17, fontWeight:700, color:T.text1 }}>待我审批</h2>
          {apps.length > 0 && <span style={{ background:T.dangerLight, color:T.danger, fontSize:11, fontWeight:700, padding:'2px 8px', borderRadius:4, border:`1px solid ${T.danger+'33'}` }}>{apps.length} 条待办</span>}
        </div>
        <div style={{ display:'flex', flexDirection:'column', gap:6, overflow:'auto', flex:1 }}>
          {apps.length === 0 ? (
            <div style={{ textAlign:'center', padding:'52px 20px', color:T.text3 }}>
              <div style={{ display:'flex', justifyContent:'center', opacity:0.25, marginBottom:10 }}><Ic n="checkCircle" size={44} color={T.text2}/></div>
              <p style={{ fontSize:13 }}>暂无待审批申请</p>
            </div>
          ) : apps.map(a => (
            <div key={a.id} onClick={() => setSel(a)} style={{ padding:'12px 14px', borderRadius:6, cursor:'pointer', border:`1px solid ${sel?.id===a.id?T.text1:T.border}`, background:sel?.id===a.id?T.surfaceWarm:T.surface, transition:'all 0.12s' }}
              onMouseEnter={e=>sel?.id!==a.id&&(e.currentTarget.style.borderColor=T.borderDark)}
              onMouseLeave={e=>sel?.id!==a.id&&(e.currentTarget.style.borderColor=T.border)}>
              <div style={{ fontSize:13, fontWeight:600, color:T.text1, marginBottom:5 }}>{a.title}</div>
              {[[a.unit,'building'],[`${a.startTime} — ${a.endTime.split(' ')[1]}`,'calendar'],[`${a.applicant} · ${a.dept}`,'user']].map(([t,ic]) => (
                <div key={ic} style={{ display:'flex', alignItems:'center', gap:5, fontSize:11, color:T.text3, marginBottom:2 }}><Ic n={ic} size={10}/>{t}</div>
              ))}
              <div style={{ marginTop:8, display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                <span style={{ fontSize:10, color:T.text3 }}>{a.created}</span>
                <span style={{ fontSize:10, background:T.warningLight, color:T.warning, padding:'2px 7px', borderRadius:3, fontWeight:600 }}>待审批</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Right detail */}
      <div style={{ flex:1, minWidth:0, overflow:'auto' }}>
        {!sel ? (
          <div style={{ display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', height:'100%', color:T.text3, gap:10 }}>
            <div style={{ opacity:0.2 }}><Ic n="clipboard" size:50 color={T.text2}/></div>
            <p style={{ fontSize:14 }}>从左侧选择申请查看详情</p>
          </div>
        ) : (
          <Card style={{ padding:'22px 24px' }}>
            <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:18 }}>
              <div><h3 style={{ margin:0, fontSize:18, fontWeight:700, color:T.text1, marginBottom:3 }}>{sel.title}</h3><MonoLabel>{sel.id}</MonoLabel></div>
              <Badge status={sel.status}/>
            </div>

            <div style={{ display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:10, marginBottom:18 }}>
              {[['来访单位',sel.unit,'building'],['参观日期',sel.startTime.split(' ')[0],'calendar'],['使用时段',`${sel.startTime.split(' ')[1]} — ${sel.endTime.split(' ')[1]}`,'clock'],['预计人数',`${sel.headCount} 人`,'users'],['最高领导',sel.leader,'user'],['申请部门',sel.dept,'briefcase']].map(([k,v,ic]) => (
                <div key={k} style={{ background:T.bg, borderRadius:6, padding:'9px 12px', display:'flex', gap:8, alignItems:'flex-start' }}>
                  <Ic n={ic} size={12} color={T.text3}/>
                  <div><MonoLabel style={{ display:'block', marginBottom:2 }}>{k}</MonoLabel><span style={{ fontSize:13, fontWeight:600, color:T.text1 }}>{v}</span></div>
                </div>
              ))}
            </div>

            {sel.agenda && <div style={{ background:T.bg, borderRadius:6, padding:'10px 14px', marginBottom:16 }}><MonoLabel style={{ display:'block', marginBottom:4 }}>简要议程</MonoLabel><p style={{ margin:0, fontSize:13, color:T.text2, lineHeight:1.7 }}>{sel.agenda}</p></div>}

            <div style={{ marginBottom:16 }}>
              <MonoLabel style={{ display:'block', marginBottom:8 }}>来访客户</MonoLabel>
              <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
                {sel.visitors.map((v,i) => (
                  <div key={i} style={{ display:'flex', alignItems:'center', gap:9, padding:'8px 12px', background:T.bg, borderRadius:6 }}>
                    <div style={{ width:30, height:30, borderRadius:'50%', background:T.text1, color:'#fff', display:'flex', alignItems:'center', justifyContent:'center', fontSize:13, fontWeight:700, flexShrink:0 }}>{v.name[0]}</div>
                    <div style={{ flex:1 }}><span style={{ fontSize:13, fontWeight:600, color:T.text1 }}>{v.name}</span><span style={{ fontSize:12, color:T.text3, marginLeft:6 }}>{v.title}</span></div>
                    {v.isStrategic && <span style={{ display:'inline-flex', alignItems:'center', gap:3, background:T.warningLight, color:T.warning, fontSize:10, fontWeight:600, padding:'2px 7px', borderRadius:3, border:`1px solid ${T.warning+'33'}` }}><Ic n="star" size={9} color={T.warning}/>{v.strategicLevel}</span>}
                  </div>
                ))}
              </div>
            </div>

            {sel.services?.length > 0 && (
              <div style={{ marginBottom:16 }}>
                <MonoLabel style={{ display:'block', marginBottom:8 }}>附加服务</MonoLabel>
                <div style={{ display:'flex', gap:6, flexWrap:'wrap' }}>{sel.services.map(s => <span key={s} style={{ fontSize:11, fontWeight:600, padding:'3px 8px', borderRadius:3, background:T.text1, color:'#fff' }}>{s}</span>)}</div>
              </div>
            )}

            <div style={{ marginBottom:20 }}>
              <MonoLabel style={{ display:'block', marginBottom:10 }}>审批进度</MonoLabel>
              <ApprovalTimeline nodes={sel.approvalNodes}/>
            </div>

            <div style={{ display:'flex', gap:8, paddingTop:14, borderTop:`1px solid ${T.border}` }}>
              <Btn variant="success" icon="check" onClick={() => setModal({ type:'approve', app:sel })}>审批通过</Btn>
              <Btn variant="danger" icon="x" onClick={() => setModal({ type:'reject', app:sel })}>驳回申请</Btn>
              <Btn variant="ghost" icon="send" onClick={() => setModal({ type:'forward', app:sel })}>转交他人</Btn>
            </div>
          </Card>
        )}
      </div>

      <Modal open={!!modal} title={modal?.type==='approve'?'确认审批通过':modal?.type==='reject'?'驳回申请':'转交审批'} onClose={() => { setModal(null); setComment(''); }}>
        {modal && (
          <div style={{ display:'flex', flexDirection:'column', gap:14 }}>
            <div style={{ background:modal.type==='approve'?T.successLight:T.dangerLight, border:`1px solid ${modal.type==='approve'?T.success+'33':T.danger+'33'}`, borderRadius:4, padding:'10px 12px', fontSize:12, color:modal.type==='approve'?T.success:T.danger, display:'flex', gap:7 }}>
              <Ic n={modal.type==='approve'?'checkCircle':'alertTri'} size={13} color={modal.type==='approve'?T.success:T.danger}/>
              {modal.type==='approve'?'通过后将自动流转至下一级，申请人收到站内通知。':'驳回后申请人可查看原因并重新提交。'}
            </div>
            <div>
              <MonoLabel style={{ display:'block', marginBottom:5 }}>审批意见{modal.type==='reject'&&<span style={{color:T.accent}}> ✱</span>}</MonoLabel>
              <textarea value={comment} onChange={e => setComment(e.target.value)}
                placeholder={modal.type==='approve'?'审批意见（选填）':'请说明驳回原因（必填）'}
                style={{ border:`1px solid ${T.border}`, borderRadius:4, padding:'8px 10px', fontSize:13, width:'100%', height:80, resize:'none', boxSizing:'border-box', outline:'none', fontFamily:'inherit' }}
                onFocus={e=>e.target.style.borderColor=T.text1} onBlur={e=>e.target.style.borderColor=T.border}/>
            </div>
            <div style={{ display:'flex', gap:8, justifyContent:'flex-end' }}>
              <Btn variant="ghost" onClick={() => { setModal(null); setComment(''); }}>取消</Btn>
              <Btn variant={modal.type==='approve'?'success':'danger'} disabled={modal.type==='reject'&&!comment.trim()} onClick={() => doAction(modal.type)}>
                {modal.type==='approve'?'确认通过':modal.type==='reject'?'确认驳回':'确认转交'}
              </Btn>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

// ============ CALENDAR VIEW ============
const CalendarView = ({ notify }) => {
  const [cur, setCur] = useState(new Date('2026-04-23'));
  const WD = ['日','一','二','三','四','五','六'];
  const getWeek = d => { const s=new Date(d); s.setDate(d.getDate()-d.getDay()); return Array.from({length:7},(_,i)=>{ const x=new Date(s); x.setDate(s.getDate()+i); return x; }); };
  const dates = getWeek(cur);
  const ds = d => d.toISOString().split('T')[0];
  const isToday = d => ds(d)==='2026-04-23';
  const hours = Array.from({length:11},(_,i)=>i+8);
  const SC = { approved:T.success, pending:T.warning };

  return (
    <div>
      <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:18 }}>
        <div style={{ display:'flex', alignItems:'center', gap:10 }}>
          <h2 style={{ margin:0, fontSize:19, fontWeight:700, color:T.text1 }}>排期日历</h2>
          <div style={{ display:'flex', gap:2 }}>
            {[['chevronLeft',-7],['chevronRight',7]].map(([ic,d])=>(
              <button key={ic} onClick={() => setCur(c=>{ const n=new Date(c); n.setDate(c.getDate()+d); return n; })}
                style={{ border:`1px solid ${T.border}`, background:T.surface, borderRadius:4, width:28, height:28, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center' }}>
                <Ic n={ic} size={13} color={T.text2}/>
              </button>
            ))}
            <div style={{ margin:'0 4px', padding:'5px 12px', background:T.bg, border:`1px solid ${T.border}`, borderRadius:4, fontSize:12, fontWeight:600, color:T.text1 }}>
              {dates[0].getMonth()+1}月 {dates[0].getDate()}日 — {dates[6].getDate()}日
            </div>
          </div>
        </div>
        <div style={{ display:'flex', gap:12, alignItems:'center' }}>
          {[['已批准',T.success],['审批中',T.warning]].map(([l,c]) => (
            <span key={l} style={{ display:'inline-flex', alignItems:'center', gap:5, fontSize:12, color:T.text2 }}>
              <span style={{ width:8, height:8, borderRadius:2, background:c }}/>{l}
            </span>
          ))}
          <Btn variant="outline" size="sm" icon="download" onClick={() => notify('正在生成导出文件…','info')}>导出</Btn>
        </div>
      </div>

      <Card style={{ overflow:'hidden' }}>
        <div style={{ display:'grid', gridTemplateColumns:'50px repeat(7,1fr)', background:T.bg, borderBottom:`2px solid ${T.border}` }}>
          <div style={{ borderRight:`1px solid ${T.border}` }}/>
          {dates.map((d,i) => (
            <div key={i} style={{ padding:'10px 6px', textAlign:'center', borderLeft:`1px solid ${T.border}`, background:isToday(d)?T.accentLight:'transparent' }}>
              <MonoLabel style={{ display:'block', marginBottom:3, color:isToday(d)?T.accent:T.text3 }}>{WD[i]}</MonoLabel>
              <div style={{ width:30, height:30, borderRadius:'50%', margin:'0 auto', background:isToday(d)?T.text1:'transparent', display:'flex', alignItems:'center', justifyContent:'center', fontSize:14, fontWeight:700, color:isToday(d)?'#fff':T.text1 }}>{d.getDate()}</div>
            </div>
          ))}
        </div>
        <div style={{ maxHeight:460, overflow:'auto' }}>
          {hours.map(h => (
            <div key={h} style={{ display:'grid', gridTemplateColumns:'50px repeat(7,1fr)', minHeight:52, borderBottom:`1px solid #f5f4f0` }}>
              <div style={{ padding:'6px 8px', fontSize:10, color:T.text3, textAlign:'right', background:T.bg, borderRight:`1px solid ${T.border}`, paddingTop:9, fontFamily:'monospace' }}>{h}:00</div>
              {dates.map((d,i) => {
                const evs = MOCK_CALENDAR.filter(e => e.start===ds(d) && parseInt(e.startTime.split(':')[0])===h);
                return (
                  <div key={i} style={{ borderLeft:`1px solid #f0ede8`, padding:'3px', background:isToday(d)?'#fffef9':'' }}>
                    {evs.map(ev => (
                      <div key={ev.id} onClick={() => notify(`${ev.title}：${ev.startTime} — ${ev.end}`,'info')}
                        style={{ background:SC[ev.status]||T.text2, color:'#fff', borderRadius:4, padding:'4px 7px', fontSize:11, fontWeight:600, cursor:'pointer', marginBottom:2, boxShadow:'0 1px 3px rgba(0,0,0,0.12)' }}>
                        <div style={{ whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis' }}>{ev.title}</div>
                        <div style={{ fontSize:9, opacity:0.8, marginTop:1, fontFamily:'monospace' }}>{ev.startTime} — {ev.end}</div>
                      </div>
                    ))}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
};

// ============ VISIT RECORD VIEW ============
const VisitRecordView = ({ notify }) => {
  const [sel, setSel] = useState(null);
  const [form, setForm] = useState({ headCount:'', oppCode:'', notes:'' });
  const [returnModal, setReturnModal] = useState(null);
  const [returnForm, setReturnForm] = useState({ person:'刘主管', time:'16:30' });
  const apps = MOCK_APPLICATIONS.filter(a => a.status==='approved' || a.status==='completed');
  const setF = (k,v) => setForm(f => ({ ...f, [k]:v }));

  return (
    <div>
      <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:18 }}>
        <div><h2 style={{ margin:0, fontSize:19, fontWeight:700, color:T.text1 }}>参观留存</h2><p style={{ margin:'3px 0 0', fontSize:12, color:T.text3 }}>录入实际到场情况、商机编码与现场照片</p></div>
      </div>
      <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
        {apps.map(app => (
          <Card key={app.id} style={{ padding:'16px 18px' }}>
            <div style={{ display:'flex', alignItems:'center', gap:12 }}>
              <div style={{ width:36, height:36, borderRadius:6, background:app.status==='completed'?T.successLight:T.bg, border:`1px solid ${app.status==='completed'?T.success+'33':T.border}`, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                <Ic n={app.status==='completed'?'checkCircle':'camera'} size:17 color={app.status==='completed'?T.success:T.text2}/>
              </div>
              <div style={{ flex:1, minWidth:0 }}>
                <div style={{ display:'flex', alignItems:'center', gap:8, marginBottom:4, flexWrap:'wrap' }}>
                  <span style={{ fontSize:14, fontWeight:700, color:T.text1 }}>{app.title}</span>
                  <Badge status={app.status}/>
                  {app.visitRecord?.returnSigned && <span style={{ display:'inline-flex', alignItems:'center', gap:3, fontSize:10, background:T.successLight, color:T.success, padding:'2px 7px', borderRadius:3, fontWeight:600 }}><Ic n="check" size={9} color={T.success}/>已归还</span>}
                  {app.visitRecord && !app.visitRecord.returnSigned && <span style={{ display:'inline-flex', alignItems:'center', gap:3, fontSize:10, background:T.warningLight, color:T.warning, padding:'2px 7px', borderRadius:3, fontWeight:600 }}><Ic n="clock" size={9} color={T.warning}/>待归还签收</span>}
                </div>
                <div style={{ display:'flex', gap:12, fontSize:11, color:T.text3, flexWrap:'wrap' }}>
                  {[[app.startTime,'calendar'],[app.unit,'building'],[`计划 ${app.headCount} 人`,'users'],...(app.opportunityCode?[[app.opportunityCode,'briefcase']]:[])].map(([t,ic]) => (
                    <span key={t} style={{ display:'inline-flex', alignItems:'center', gap:4 }}><Ic n={ic} size={11}/>{t}</span>
                  ))}
                </div>
              </div>
              <div style={{ display:'flex', gap:6, flexShrink:0 }}>
                {app.visitRecord && !app.visitRecord.returnSigned && (
                  <Btn variant="outline" size="sm" icon="checkSquare" onClick={() => setReturnModal(app)}>归还签收</Btn>
                )}
                <Btn variant={app.visitRecord?'ghost':'primary'} size="sm" icon={app.visitRecord?'edit':'plus'}
                  onClick={() => { setSel(app); setForm({ headCount:app.visitRecord?.actualHeadCount||'', oppCode:app.opportunityCode||'', notes:'' }); }}>
                  {app.visitRecord?'编辑':'录入留存'}
                </Btn>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Visit record modal */}
      <Modal open={!!sel} title="参观留存录入" onClose={() => setSel(null)} width={520}>
        {sel && (
          <div style={{ display:'flex', flexDirection:'column', gap:14 }}>
            <div style={{ background:T.bg, borderRadius:6, padding:'10px 14px', display:'flex', gap:9 }}>
              <Ic n="building" size={14} color={T.text3}/>
              <div><div style={{ fontSize:13, fontWeight:700, color:T.text1 }}>{sel.title}</div><div style={{ fontSize:11, color:T.text3, marginTop:2 }}>{sel.startTime} · {sel.unit}</div></div>
            </div>
            <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:12 }}>
              <FancyInput label="实际到场人数" value={form.headCount} onChange={v=>setF('headCount',v)} placeholder={`计划${sel.headCount}人`} type="number" required/>
              <FancyInput label="商机编码" value={form.oppCode} onChange={v=>setF('oppCode',v)} placeholder="填「无」可后续补录" hint="允许事后补录"/>
              <FancyInput label="实际开始时间" value={form.actualStart||sel.startTime} onChange={v=>setF('actualStart',v)}/>
              <FancyInput label="实际结束时间" value={form.actualEnd||sel.endTime} onChange={v=>setF('actualEnd',v)}/>
            </div>
            <div>
              <MonoLabel style={{ display:'block', marginBottom:8 }}>现场照片</MonoLabel>
              <div style={{ border:`2px dashed ${T.border}`, borderRadius:6, padding:'24px', textAlign:'center', cursor:'pointer', background:T.bg, transition:'border-color 0.15s' }}
                onClick={() => notify('请选择照片（演示模式）','info')}
                onMouseEnter={e=>e.currentTarget.style.borderColor=T.text1}
                onMouseLeave={e=>e.currentTarget.style.borderColor=T.border}>
                <div style={{ display:'flex', justifyContent:'center', marginBottom:7, opacity:0.4 }}><Ic n="upload" size:22 color={T.text2}/></div>
                <div style={{ fontSize:12, color:T.text2, fontWeight:500 }}>点击上传或拍摄照片，支持多张</div>
                <MonoLabel style={{ display:'block', marginTop:4 }}>JPG / PNG · 单张 ≤ 10 MB</MonoLabel>
              </div>
              {sel.visitRecord?.photos > 0 && (
                <div style={{ display:'flex', gap:8, marginTop:10 }}>
                  {Array.from({length:sel.visitRecord.photos}).map((_,i) => (
                    <div key={i} style={{ width:64, height:64, borderRadius:6, background:T.surfaceWarm, border:`1px solid ${T.border}`, display:'flex', alignItems:'center', justifyContent:'center', opacity:0.7 }}>
                      <Ic n="camera" size:20 color={T.text3}/>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <FancyInput label="备注">
              <textarea value={form.notes} onChange={e=>setF('notes',e.target.value)} placeholder="参观情况说明、客户反馈等"
                style={{ border:`1px solid ${T.border}`, borderRadius:4, padding:'8px 10px', fontSize:13, width:'100%', height:70, resize:'none', boxSizing:'border-box', outline:'none', fontFamily:'inherit' }}/>
            </FancyInput>
            <div style={{ display:'flex', gap:8, justifyContent:'flex-end' }}>
              <Btn variant="ghost" onClick={() => setSel(null)}>取消</Btn>
              <Btn variant="primary" icon="check" onClick={() => { setSel(null); notify('参观留存已提交','success'); }}>确认留存</Btn>
            </div>
          </div>
        )}
      </Modal>

      {/* Return sign-off modal */}
      <Modal open={!!returnModal} title="归还签收" onClose={() => setReturnModal(null)} width=420>
        {returnModal && (
          <div style={{ display:'flex', flexDirection:'column', gap:14 }}>
            <div style={{ background:T.bg, borderRadius:6, padding:'10px 14px', fontSize:12 }}>
              <b style={{ color:T.text1 }}>{returnModal.title}</b>
              <div style={{ color:T.text3, marginTop:2 }}>{returnModal.startTime} — {returnModal.endTime.split(' ')[1]}</div>
            </div>
            <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:12 }}>
              <FancyInput label="签收人" value={returnForm.person} onChange={v=>setReturnForm(f=>({...f,person:v}))} required/>
              <FancyInput label="归还时间" value={returnForm.time} onChange={v=>setReturnForm(f=>({...f,time:v}))} placeholder="HH:MM" required/>
            </div>
            <div style={{ background:T.successLight, border:`1px solid ${T.success+'33'}`, borderRadius:4, padding:'10px 12px', fontSize:12, color:T.success, display:'flex', gap:7 }}>
              <Ic n="checkCircle" size={13} color={T.success}/>
              <span>签收后将记录归还时间，并通知申请人</span>
            </div>
            <div style={{ display:'flex', gap:8, justifyContent:'flex-end' }}>
              <Btn variant="ghost" onClick={() => setReturnModal(null)}>取消</Btn>
              <Btn variant="success" icon="checkSquare" onClick={() => { setReturnModal(null); notify('归还签收已记录','success'); }}>确认签收</Btn>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

// ============ DASHBOARD VIEW ============
const DashboardView = ({ notify, onExport }) => {
  const monthlyData = [
    {label:'1月',v1:8,v2:6},{label:'2月',v1:5,v2:9},{label:'3月',v1:12,v2:8},
    {label:'4月',v1:18,v2:11},{label:'5月',v1:0,v2:14},{label:'6月',v1:0,v2:10},
  ];

  const cityData = [
    {city:'天河区',count:8},{city:'越秀区',count:5},{city:'海珠区',count:4},
    {city:'黄埔区',count:3},{city:'番禺区',count:3},{city:'白云区',count:2},
    {city:'荔湾区',count:2},{city:'花都区',count:1},
  ];
  const maxCity = Math.max(...cityData.map(d=>d.count));

  const kpis = [
    { label:'本月参观场次', value:'18', unit:'场', up:true, change:'+3', icon:'building' },
    { label:'战客覆盖率',   value:'62', unit:'%',  up:true, change:'+12%', icon:'star', target:62 },
    { label:'待审批申请',   value:'4',  unit:'条',  icon:'clock' },
    { label:'商机触达数',   value:'23', unit:'条',  up:true, change:'+5',  icon:'briefcase' },
  ];

  const industryData = [
    {name:'信息技术',count:5,c:T.accent},{name:'政府机构',count:4,c:'#1d4ed8'},
    {name:'金融保险',count:3,c:T.warning+'cc'},{name:'建筑工程',count:2,c:'#7e22ce'},
    {name:'制造业',count:2,c:T.danger},{name:'其他',count:2,c:T.text3},
  ];

  const funnelData = [
    {stage:'参观接待',count:36},{stage:'商机触达',count:23},{stage:'商机跟进',count:14},{stage:'签约转化',count:7},
  ];
  const funnelColors = [T.text1,'#313130','#626260','#9c9fa5'];

  const strategicData = [
    {level:'省直管战客',count:6,c:T.danger},{level:'市直管战客',count:10,c:T.warning},
    {level:'区县直管战客',count:8,c:'#1d4ed8'},{level:'非战略客户',count:12,c:T.text3},
  ];
  const totalS = strategicData.reduce((s,d)=>s+d.count,0);

  return (
    <div style={{ display:'flex', flexDirection:'column', gap:18 }}>
      <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between' }}>
        <div><h2 style={{ margin:0, fontSize:19, fontWeight:700, color:T.text1 }}>统计看板</h2><p style={{ margin:'3px 0 0', fontSize:12, color:T.text3 }}>2026年度 · 数据截至 4月23日</p></div>
        <div style={{ display:'flex', gap:6 }}>
          <Btn variant="ghost" size="sm" icon="fileText" onClick={onExport}>导出预览</Btn>
          <Btn variant="outline" size="sm" icon="download" onClick={() => notify('正在导出省公司报表…','info')}>导出Excel</Btn>
        </div>
      </div>

      {/* KPI row */}
      <div style={{ display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:12 }}>
        {kpis.map((k,i) => (
          <Card key={i} style={{ padding:'18px' }}>
            <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:12 }}>
              <div style={{ width:34, height:34, borderRadius:6, background:T.bg, border:`1px solid ${T.border}`, display:'flex', alignItems:'center', justifyContent:'center' }}>
                <Ic n={k.icon} size:16 color={T.text2}/>
              </div>
              {k.change && <span style={{ fontSize:10, fontWeight:700, color:k.up?T.success:T.danger, background:k.up?T.successLight:T.dangerLight, padding:'2px 6px', borderRadius:3 }}>↑ {k.change}</span>}
            </div>
            <div style={{ display:'flex', alignItems:'flex-end', gap:3, marginBottom:3 }}>
              <span style={{ fontSize:28, fontWeight:800, color:T.text1, lineHeight:1 }}>{k.value}</span>
              <span style={{ fontSize:12, color:T.text3, marginBottom:3 }}>{k.unit}</span>
            </div>
            <MonoLabel>{k.label}</MonoLabel>
            {k.target && (
              <div style={{ marginTop:10 }}>
                <div style={{ display:'flex', justifyContent:'space-between', fontSize:10, color:T.text3, marginBottom:3 }}>
                  <span>目标 ≥50%</span><span style={{ color:T.success, fontWeight:700 }}>已达标</span>
                </div>
                <div style={{ height:4, background:T.bg, borderRadius:2, border:`1px solid ${T.border}`, overflow:'hidden' }}>
                  <div style={{ height:'100%', width:`${k.target}%`, background:T.accent, borderRadius:2, transition:'width 0.5s' }}/>
                </div>
              </div>
            )}
          </Card>
        ))}
      </div>

      {/* Line chart + industry */}
      <div style={{ display:'grid', gridTemplateColumns:'2fr 1fr', gap:16 }}>
        <Card style={{ padding:'20px 22px' }}>
          <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:16 }}>
            <div style={{ fontSize:14, fontWeight:700, color:T.text1 }}>月度参观场次</div>
            <div style={{ display:'flex', gap:10 }}>
              {[['今年',T.accent],['去年',T.border]].map(([l,c])=>(
                <span key={l} style={{ display:'inline-flex', alignItems:'center', gap:4, fontSize:11, color:T.text2 }}>
                  <span style={{ width:16, height:2, background:c, display:'inline-block', borderRadius:1 }}/>{l}
                </span>
              ))}
            </div>
          </div>
          <SVGLineChart data={monthlyData} h={156}/>
        </Card>

        <Card style={{ padding:'20px 22px' }}>
          <div style={{ fontSize:14, fontWeight:700, color:T.text1, marginBottom:16 }}>行业分布</div>
          <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
            {industryData.map((d,i) => (
              <div key={i} style={{ display:'flex', alignItems:'center', gap:7 }}>
                <span style={{ width:8, height:8, borderRadius:2, background:d.c, flexShrink:0 }}/>
                <span style={{ fontSize:12, color:T.text2, flex:1, minWidth:0 }}>{d.name}</span>
                <div style={{ width:64, height:4, background:T.bg, borderRadius:2, overflow:'hidden', border:`1px solid ${T.border}` }}>
                  <div style={{ height:'100%', width:`${(d.count/5)*100}%`, background:d.c, borderRadius:2 }}/>
                </div>
                <span style={{ fontSize:11, color:T.text3, width:14, textAlign:'right' }}>{d.count}</span>
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* City + Strategic + Funnel */}
      <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:16 }}>
        <Card style={{ padding:'20px 22px' }}>
          <div style={{ fontSize:14, fontWeight:700, color:T.text1, marginBottom:14 }}>地市分布</div>
          <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
            {cityData.map((d,i) => (
              <div key={i} style={{ display:'flex', alignItems:'center', gap:7 }}>
                <span style={{ fontSize:11, color:T.text2, width:44, flexShrink:0 }}>{d.city}</span>
                <div style={{ flex:1, height:5, background:T.bg, borderRadius:2, overflow:'hidden', border:`1px solid ${T.border}` }}>
                  <div style={{ height:'100%', width:`${(d.count/maxCity)*100}%`, background:i<3?T.accent:T.text3, borderRadius:2, transition:'width 0.5s' }}/>
                </div>
                <span style={{ fontSize:11, fontWeight:700, color:T.text1, width:16, textAlign:'right', flexShrink:0 }}>{d.count}</span>
              </div>
            ))}
          </div>
        </Card>

        <Card style={{ padding:'20px 22px' }}>
          <div style={{ fontSize:14, fontWeight:700, color:T.text1, marginBottom:14 }}>战客级别</div>
          <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
            {strategicData.map((d,i) => (
              <div key={i}>
                <div style={{ display:'flex', justifyContent:'space-between', fontSize:11, marginBottom:4 }}>
                  <span style={{ color:T.text2 }}>{d.level}</span>
                  <span style={{ fontWeight:700, color:T.text1 }}>{d.count} <span style={{ color:T.text3, fontWeight:400 }}>({Math.round(d.count/totalS*100)}%)</span></span>
                </div>
                <div style={{ height:5, background:T.bg, borderRadius:2, overflow:'hidden', border:`1px solid ${T.border}` }}>
                  <div style={{ height:'100%', width:`${(d.count/totalS)*100}%`, background:d.c, borderRadius:2 }}/>
                </div>
              </div>
            ))}
          </div>
        </Card>

        <Card style={{ padding:'20px 22px' }}>
          <div style={{ fontSize:14, fontWeight:700, color:T.text1, marginBottom:14 }}>商机转化漏斗</div>
          <div style={{ display:'flex', flexDirection:'column', gap:7 }}>
            {funnelData.map((d,i) => (
              <div key={i} style={{ display:'flex', alignItems:'center', gap:8 }}>
                <div style={{ height:34, background:funnelColors[i], borderRadius:4, display:'flex', alignItems:'center', paddingLeft:10, width:`${(d.count/funnelData[0].count)*100}%`, minWidth:72, transition:'width 0.4s' }}>
                  <span style={{ color:'#fff', fontSize:11, fontWeight:600, whiteSpace:'nowrap' }}>{d.stage}</span>
                </div>
                <span style={{ fontSize:13, fontWeight:700, color:T.text1, flexShrink:0 }}>{d.count}</span>
              </div>
            ))}
            <div style={{ paddingTop:10, borderTop:`1px solid ${T.border}`, display:'flex', justifyContent:'space-between', fontSize:11 }}>
              <span style={{ color:T.text3 }}>参观→签约转化率</span>
              <span style={{ fontWeight:700, color:T.success }}>19.4%</span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

Object.assign(window, { ApprovalView, CalendarView, VisitRecordView, DashboardView });
