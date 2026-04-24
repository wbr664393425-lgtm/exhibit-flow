
// ApplyView — 新样式 + H5 手机视图 + 取消/改期流程
const { useState, useCallback } = React;

const SERVICES = ['横幅打印','茶水','合影','摄影','车辆接送'];
const INDUSTRIES = ['信息技术','政府机构','金融保险','建筑工程','制造业','能源电力','互联网','教育医疗','商业零售','其他'];
const DISTRICTS = ['天河区','越秀区','荔湾区','海珠区','白云区','黄埔区','番禺区','花都区','增城区','从化区','南沙区'];
const LEADERS = ['无','部门副总','分管副总','总经理'];
const DEPTS = ['政企客户部','公共事业部','家庭客户部','市场部','综合部'];

// ── Step Bar ─────────────────────────────────────────────────────
const StepBar = ({ steps, cur }) => (
  <div style={{ display:'flex', alignItems:'center', marginBottom:24 }}>
    {steps.map((s,i)=>{
      const done=i+1<cur, active=i+1===cur;
      return (
        <React.Fragment key={i}>
          <div style={{ display:'flex', alignItems:'center', gap:8 }}>
            <div style={{ width:26, height:26, borderRadius:'50%', display:'flex', alignItems:'center', justifyContent:'center', fontSize:11, fontWeight:700, flexShrink:0, border:`1.5px solid ${done?T.success:active?T.text1:T.border}`, background:done?T.success:active?T.text1:T.surface, color:done||active?'#fff':T.text3, transition:'all 0.2s' }}>
              {done?<Ic n="check" size={11} color="#fff"/>:i+1}
            </div>
            <span style={{ fontSize:12, fontWeight:active?700:400, color:active?T.text1:T.text3, whiteSpace:'nowrap' }}>{s}</span>
          </div>
          {i<steps.length-1 && <div style={{ flex:1, height:1, background:done?T.success:T.border, margin:'0 10px', transition:'background 0.2s' }}/>}
        </React.Fragment>
      );
    })}
  </div>
);

// ── New Application Form ──────────────────────────────────────────
const NewAppForm = ({ onSubmit, onCancel, notify }) => {
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({ title:'', unit:'', industry:INDUSTRIES[0], district:DISTRICTS[0], applicant:'李建国', phone:'13812345678', dept:DEPTS[0], startDate:'2026-05-06', startHour:'09', endHour:'11', leader:LEADERS[0], headCount:'', agenda:'', notes:'', services:[], visitors:[{unit:'',name:'',title:'',isStrategic:false,strategicLevel:''}], conflictChecked:false, conflictResult:null });
  const [checking, setChecking] = useState(false);
  const set=(k,v)=>setForm(f=>({...f,[k]:v}));
  const G2={display:'grid',gridTemplateColumns:'1fr 1fr',gap:14};
  const STEPS=['基本信息','来访客户','服务需求','确认提交'];

  const checkConflict=()=>{
    setChecking(true);
    setTimeout(()=>{
      const c=form.startDate==='2026-04-25'&&parseInt(form.startHour)<12;
      setForm(f=>({...f,conflictChecked:true,conflictResult:c?'2026-04-25 09:00–11:30 已被「华为技术参观」占用':null}));
      setChecking(false);
    },900);
  };

  const addV=()=>setForm(f=>({...f,visitors:[...f.visitors,{unit:'',name:'',title:'',isStrategic:false,strategicLevel:''}]}));
  const rmV=i=>setForm(f=>({...f,visitors:f.visitors.filter((_,j)=>j!==i)}));
  const setV=(i,k,v)=>setForm(f=>{const vs=[...f.visitors];vs[i]={...vs[i],[k]:v};return{...f,visitors:vs};});
  const togS=s=>set('services',form.services.includes(s)?form.services.filter(x=>x!==s):[...form.services,s]);

  const renderStep=()=>{
    if(step===1) return (
      <div style={{display:'flex',flexDirection:'column',gap:14}}>
        <FancyInput label="会议主题" value={form.title} onChange={v=>set('title',v)} placeholder="本次参观的主题" required/>
        <div style={G2}>
          <FancyInput label="来访单位" value={form.unit} onChange={v=>set('unit',v)} placeholder="单位全称" required/>
          <FancySelect label="所属行业" value={form.industry} onChange={v=>set('industry',v)} options={INDUSTRIES}/>
        </div>
        <div style={G2}>
          <FancyInput label="参观日期" required>
            <input type="date" value={form.startDate} onChange={e=>set('startDate',e.target.value)}
              style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 10px',fontSize:13,width:'100%',boxSizing:'border-box',outline:'none',fontFamily:'inherit'}}
              onFocus={e=>e.target.style.borderColor=T.text1} onBlur={e=>e.target.style.borderColor=T.border}/>
          </FancyInput>
          <FancyInput label="使用时段" required>
            <div style={{display:'flex',alignItems:'center',gap:6}}>
              {['startHour','endHour'].map((k,idx)=>(
                <React.Fragment key={k}>
                  {idx>0&&<span style={{color:T.text3,fontSize:11,flexShrink:0}}>—</span>}
                  <select value={form[k]} onChange={e=>set(k,e.target.value)}
                    style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 8px',fontSize:13,flex:1,outline:'none',fontFamily:'inherit',appearance:'none',textAlign:'center'}}>
                    {Array.from({length:12},(_,i)=>i+(k==='startHour'?8:9)).map(h=><option key={h} value={String(h).padStart(2,'0')}>{String(h).padStart(2,'0')}:00</option>)}
                  </select>
                </React.Fragment>
              ))}
            </div>
          </FancyInput>
        </div>
        {/* Conflict check */}
        <div style={{display:'flex',alignItems:'center',gap:10,padding:'10px 12px',borderRadius:4,background:!form.conflictChecked?T.bg:form.conflictResult?T.dangerLight:T.successLight,border:`1px solid ${!form.conflictChecked?T.border:form.conflictResult?T.danger+'44':T.success+'44'}`}}>
          <Ic n={!form.conflictChecked?'search':form.conflictResult?'alertTri':'checkCircle'} size={14} color={!form.conflictChecked?T.text3:form.conflictResult?T.danger:T.success}/>
          <span style={{flex:1,fontSize:12,color:!form.conflictChecked?T.text2:form.conflictResult?T.danger:T.success}}>
            {!form.conflictChecked?'建议填写日期后检测时段冲突':form.conflictResult?`冲突：${form.conflictResult}`:'时段可用，无冲突'}
          </span>
          <Btn variant="ghost" size="sm" onClick={checkConflict} disabled={checking||!form.startDate}>{checking?'检测中…':'冲突检测'}</Btn>
        </div>
        <div style={G2}>
          <FancySelect label="最高陪同领导" value={form.leader} onChange={v=>set('leader',v)} options={LEADERS}/>
          <FancyInput label="预计人数" value={form.headCount} onChange={v=>set('headCount',v)} placeholder="总人数" type="number"/>
        </div>
        <div style={G2}>
          <FancySelect label="所属区县" value={form.district} onChange={v=>set('district',v)} options={DISTRICTS} required/>
          <FancySelect label="申请部门" value={form.dept} onChange={v=>set('dept',v)} options={DEPTS} required/>
        </div>
        <div style={G2}>
          <FancyInput label="申请人姓名" value={form.applicant} onChange={v=>set('applicant',v)} required/>
          <FancyInput label="联系电话" value={form.phone} onChange={v=>set('phone',v)} required/>
        </div>
        <FancyInput label="简要议程">
          <textarea value={form.agenda} onChange={e=>set('agenda',e.target.value)} placeholder="本次参观主要议程和目的"
            style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 10px',fontSize:13,width:'100%',height:72,resize:'vertical',boxSizing:'border-box',outline:'none',fontFamily:'inherit'}}
            onFocus={e=>e.target.style.borderColor=T.text1} onBlur={e=>e.target.style.borderColor=T.border}/>
        </FancyInput>
      </div>
    );
    if(step===2) return (
      <div style={{display:'flex',flexDirection:'column',gap:12}}>
        <div style={{display:'flex',alignItems:'center',justifyContent:'space-between'}}>
          <span style={{fontSize:13,color:T.text2}}>每位来访客户单独填写一行</span>
          <Btn variant="outline" size="sm" icon="plus" onClick={addV}>添加客户</Btn>
        </div>
        {form.visitors.map((v,i)=>(
          <div key={i} style={{background:T.bg,borderRadius:6,padding:'14px 14px',border:`1px solid ${T.border}`}}>
            <div style={{display:'flex',alignItems:'center',justifyContent:'space-between',marginBottom:12}}>
              <MonoLabel style={{color:T.accent}}>客户 {i+1}</MonoLabel>
              {i>0&&<button onClick={()=>rmV(i)} style={{border:'none',background:'none',cursor:'pointer',color:T.text3,display:'flex',padding:4}}><Ic n="x" size={13}/></button>}
            </div>
            <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:10,marginBottom:10}}>
              <FancyInput label="来访单位" value={v.unit} onChange={val=>setV(i,'unit',val)} required placeholder="客户单位"/>
              <FancyInput label="姓名" value={v.name} onChange={val=>setV(i,'name',val)} required placeholder="客户姓名"/>
              <FancyInput label="职务" value={v.title} onChange={val=>setV(i,'title',val)} placeholder="职务/职称"/>
              <FancyInput label="是否战略客户">
                <div style={{display:'flex',gap:16,paddingTop:7}}>
                  {['是','否'].map(o=>(
                    <label key={o} style={{display:'flex',alignItems:'center',gap:5,cursor:'pointer',fontSize:13,color:T.text1}}>
                      <input type="radio" checked={v.isStrategic===(o==='是')} onChange={()=>setV(i,'isStrategic',o==='是')}/>{o}
                    </label>
                  ))}
                </div>
              </FancyInput>
            </div>
            {v.isStrategic&&<FancySelect label="战客级别" value={v.strategicLevel} onChange={val=>setV(i,'strategicLevel',val)} options={['省直管战客','市直管战客','区县直管战客','其他战客']}/>}
          </div>
        ))}
      </div>
    );
    if(step===3) return (
      <div style={{display:'flex',flexDirection:'column',gap:18}}>
        <FancyInput label="附加服务（可多选）">
          <div style={{display:'flex',flexWrap:'wrap',gap:8,paddingTop:6}}>
            {SERVICES.map(s=>{
              const on=form.services.includes(s);
              return (
                <label key={s} onClick={()=>togS(s)} style={{display:'flex',alignItems:'center',gap:7,padding:'7px 14px',borderRadius:4,cursor:'pointer',border:`1px solid ${on?T.text1:T.border}`,background:on?T.text1:T.surface,color:on?'#fff':T.text2,fontSize:13,fontWeight:on?600:400,transition:'all 0.12s'}}>
                  <div style={{width:14,height:14,borderRadius:3,border:`1.5px solid ${on?'#fff':T.border}`,background:on?T.accent:'transparent',display:'flex',alignItems:'center',justifyContent:'center',flexShrink:0,transition:'all 0.12s'}}>
                    {on&&<Ic n="check" size={9} color="#fff"/>}
                  </div>
                  {s}
                </label>
              );
            })}
          </div>
        </FancyInput>
        <div style={{background:T.warningLight,border:`1px solid ${T.warning+'33'}`,borderRadius:4,padding:'10px 14px',fontSize:12,color:T.warning,display:'flex',gap:8,alignItems:'flex-start'}}>
          <Ic n="info" size={14} color={T.warning}/>
          <span><b>审批链：</b>{form.leader==='总经理'?'部门领导 → 展厅主管 → 总经理（三级）':form.leader==='分管副总'?'部门领导 → 展厅主管 → 分管副总（三级）':'部门领导 → 展厅主管（二级）'}</span>
        </div>
        <FancyInput label="备注">
          <textarea value={form.notes} onChange={e=>set('notes',e.target.value)} placeholder="其他特殊需求或说明"
            style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 10px',fontSize:13,width:'100%',height:80,resize:'vertical',boxSizing:'border-box',outline:'none',fontFamily:'inherit'}}/>
        </FancyInput>
      </div>
    );
    if(step===4) return (
      <div style={{display:'flex',flexDirection:'column',gap:10}}>
        <div style={{background:T.successLight,border:`1px solid ${T.success+'44'}`,borderRadius:4,padding:'10px 14px',fontSize:12,color:T.success,display:'flex',gap:8,alignItems:'center'}}>
          <Ic n="checkCircle" size={14} color={T.success}/>
          <span>信息确认无误后点击提交，将自动发起审批流程</span>
        </div>
        <div style={{border:`1px solid ${T.border}`,borderRadius:6,overflow:'hidden'}}>
          {[['会议主题',form.title],['来访单位',form.unit],['所属行业',form.industry],['参观日期',form.startDate],['使用时段',`${form.startHour}:00 — ${form.endHour}:00`],['最高领导',form.leader],['预计人数',form.headCount||'—'],['申请部门',form.dept],['申请人',`${form.applicant}（${form.district}）`],['联系电话',form.phone],['附加服务',form.services.join('、')||'无'],['来访客户',form.visitors.map(v=>`${v.name}/${v.unit}`).join('，')]].map(([k,v],i)=>(
            <div key={k} style={{display:'flex',gap:14,padding:'9px 14px',background:i%2===0?T.bg:T.surface,fontSize:12}}>
              <span style={{color:T.text3,width:66,flexShrink:0,fontWeight:500}}>{k}</span>
              <span style={{color:T.text1,fontWeight:500}}>{v}</span>
            </div>
          ))}
        </div>
      </div>
    );
  };

  return (
    <div style={{display:'flex',flexDirection:'column',height:'100%'}}>
      <StepBar steps={STEPS} cur={step}/>
      <div style={{flex:1,overflow:'auto'}}>{renderStep()}</div>
      <div style={{display:'flex',justifyContent:'space-between',marginTop:20,paddingTop:16,borderTop:`1px solid ${T.border}`}}>
        <Btn variant="ghost" icon="arrowLeft" onClick={step===1?onCancel:()=>setStep(s=>s-1)}>{step===1?'取消':'上一步'}</Btn>
        <div style={{display:'flex',gap:8}}>
          {step<4&&<Btn variant="ghost" onClick={()=>notify('草稿已保存','info')}>存草稿</Btn>}
          {step<4
            ?<Btn variant="primary" icon="chevronRight" onClick={()=>{if(step===1&&(!form.title||!form.unit)){notify('请填写必填项','error');return;}if(step===2&&!form.visitors.every(v=>v.name&&v.unit)){notify('请完善客户信息','error');return;}setStep(s=>s+1);}}>下一步</Btn>
            :<Btn variant="orange" icon="send" onClick={()=>onSubmit(form)}>提交申请</Btn>
          }
        </div>
      </div>
    </div>
  );
};

// ── Application Detail (with Cancel / Reschedule) ─────────────────
const AppDetail = ({ app, onClose, onCancel, onReschedule }) => {
  const [tab, setTab] = useState('info');
  if(!app) return null;
  return (
    <Modal open={!!app} title={app.title} onClose={onClose} width={640}>
      <div style={{display:'flex',gap:6,marginBottom:18,borderBottom:`1px solid ${T.border}`,paddingBottom:14,flexWrap:'wrap',alignItems:'center'}}>
        {[['info','申请信息'],['approval','审批进度'],['visitors','来访客户']].map(([k,l])=>(
          <button key={k} onClick={()=>setTab(k)} style={{border:'none',background:tab===k?T.text1:'transparent',color:tab===k?'#fff':T.text2,padding:'5px 12px',borderRadius:4,cursor:'pointer',fontSize:12,fontWeight:600,transition:'all 0.12s'}}>{l}</button>
        ))}
        <div style={{flex:1}}/>
        <Badge status={app.status}/>
      </div>

      {tab==='info'&&(
        <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:12}}>
          {[['申请编号',app.id,'clipboard'],['来访单位',app.unit,'building'],['使用时段',`${app.startTime} — ${app.endTime.split(' ')[1]}`,'clock'],['预计人数',`${app.headCount} 人`,'users'],['最高领导',app.leader,'user'],['申请部门',app.dept,'briefcase'],['申请人',app.applicant,'user'],['联系电话',app.phone,'phone']].map(([k,v,ic])=>(
            <div key={k} style={{display:'flex',gap:9,padding:'10px 12px',background:T.bg,borderRadius:6,alignItems:'flex-start'}}>
              <Ic n={ic} size={13} color={T.text3}/>
              <div><MonoLabel style={{display:'block',marginBottom:3}}>{k}</MonoLabel><span style={{fontSize:13,fontWeight:600,color:T.text1}}>{v}</span></div>
            </div>
          ))}
          {app.agenda&&<div style={{gridColumn:'1/-1',padding:'10px 12px',background:T.bg,borderRadius:6}}><MonoLabel style={{display:'block',marginBottom:4}}>简要议程</MonoLabel><p style={{margin:0,fontSize:12,color:T.text2,lineHeight:1.6}}>{app.agenda}</p></div>}
          {app.services?.length>0&&<div style={{gridColumn:'1/-1'}}><MonoLabel style={{display:'block',marginBottom:8}}>附加服务</MonoLabel><div style={{display:'flex',gap:6,flexWrap:'wrap'}}>{app.services.map(s=><span key={s} style={{fontSize:11,fontWeight:600,padding:'3px 8px',borderRadius:4,background:T.text1,color:'#fff'}}>{s}</span>)}</div></div>}
        </div>
      )}
      {tab==='approval'&&<ApprovalTimeline nodes={app.approvalNodes}/>}
      {tab==='visitors'&&(
        <div style={{display:'flex',flexDirection:'column',gap:8}}>
          {app.visitors.map((v,i)=>(
            <div key={i} style={{display:'flex',alignItems:'center',gap:10,padding:'10px 12px',background:T.bg,borderRadius:6}}>
              <div style={{width:32,height:32,borderRadius:'50%',background:T.text1,color:'#fff',display:'flex',alignItems:'center',justifyContent:'center',fontSize:14,fontWeight:700,flexShrink:0}}>{v.name[0]}</div>
              <div style={{flex:1}}><span style={{fontSize:13,fontWeight:600,color:T.text1}}>{v.name}</span><span style={{fontSize:12,color:T.text2,marginLeft:6}}>{v.title}</span></div>
              {v.isStrategic&&<span style={{display:'inline-flex',alignItems:'center',gap:4,background:T.warningLight,color:T.warning,fontSize:11,fontWeight:600,padding:'2px 8px',borderRadius:4,border:`1px solid ${T.warning+'33'}`}}><Ic n="star" size={10} color={T.warning}/>{v.strategicLevel}</span>}
            </div>
          ))}
        </div>
      )}

      {/* Action bar */}
      {(app.status==='pending'||app.status==='approved')&&(
        <div style={{marginTop:20,paddingTop:14,borderTop:`1px solid ${T.border}`,display:'flex',gap:8}}>
          {app.status==='pending'&&<Btn variant="outline" size="sm" icon="rotate" onClick={()=>{onClose();onReschedule(app);}}>申请改期</Btn>}
          <Btn variant="ghost" size="sm" icon="xCircle" onClick={()=>{onClose();onCancel(app);}} style={{color:T.danger,borderColor:T.danger+'44'}}>取消申请</Btn>
        </div>
      )}
    </Modal>
  );
};

// ── Cancel Modal ──────────────────────────────────────────────────
const CancelModal = ({ app, onClose, onConfirm }) => {
  const [reason, setReason] = useState('');
  if(!app) return null;
  return (
    <Modal open={!!app} title="取消申请" onClose={onClose} width={440}>
      <div style={{display:'flex',flexDirection:'column',gap:14}}>
        <div style={{background:T.dangerLight,border:`1px solid ${T.danger+'33'}`,borderRadius:4,padding:'10px 12px',fontSize:12,color:T.danger,display:'flex',gap:8}}>
          <Ic n="alertTri" size={14} color={T.danger}/>
          <span>取消后排期将释放，已审批的记录保留存档</span>
        </div>
        <div style={{background:T.bg,borderRadius:6,padding:'10px 12px',fontSize:12,color:T.text2}}><b style={{color:T.text1}}>{app.title}</b><br/>{app.startTime} — {app.endTime.split(' ')[1]}</div>
        <FancyInput label="取消原因" required>
          <textarea value={reason} onChange={e=>setReason(e.target.value)} placeholder="请说明取消原因（必填）"
            style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 10px',fontSize:13,width:'100%',height:80,resize:'none',boxSizing:'border-box',outline:'none',fontFamily:'inherit'}}
            onFocus={e=>e.target.style.borderColor=T.text1} onBlur={e=>e.target.style.borderColor=T.border}/>
        </FancyInput>
        <div style={{display:'flex',gap:8,justifyContent:'flex-end'}}>
          <Btn variant="ghost" onClick={onClose}>保留申请</Btn>
          <Btn variant="danger" disabled={!reason.trim()} onClick={()=>onConfirm(app,reason)}>确认取消</Btn>
        </div>
      </div>
    </Modal>
  );
};

// ── Reschedule Modal ──────────────────────────────────────────────
const RescheduleModal = ({ app, onClose, onConfirm }) => {
  const [newDate, setNewDate] = useState('2026-05-08');
  const [newSH, setNewSH] = useState('09');
  const [newEH, setNewEH] = useState('11');
  if(!app) return null;
  return (
    <Modal open={!!app} title="申请改期" onClose={onClose} width={460}>
      <div style={{display:'flex',flexDirection:'column',gap:14}}>
        <div style={{background:T.bg,borderRadius:6,padding:'10px 12px'}}>
          <MonoLabel style={{display:'block',marginBottom:4}}>原时段</MonoLabel>
          <span style={{fontSize:13,fontWeight:600,color:T.text2}}>{app.startTime} — {app.endTime.split(' ')[1]}</span>
        </div>
        <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:12}}>
          <FancyInput label="新参观日期" required>
            <input type="date" value={newDate} onChange={e=>setNewDate(e.target.value)}
              style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 10px',fontSize:13,width:'100%',boxSizing:'border-box',outline:'none',fontFamily:'inherit'}}
              onFocus={e=>e.target.style.borderColor=T.text1} onBlur={e=>e.target.style.borderColor=T.border}/>
          </FancyInput>
          <FancyInput label="新使用时段" required>
            <div style={{display:'flex',alignItems:'center',gap:5}}>
              <select value={newSH} onChange={e=>setNewSH(e.target.value)} style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 8px',fontSize:13,flex:1,outline:'none',fontFamily:'inherit'}}>
                {Array.from({length:12},(_,i)=>i+8).map(h=><option key={h} value={String(h).padStart(2,'0')}>{String(h).padStart(2,'0')}:00</option>)}
              </select>
              <span style={{color:T.text3,fontSize:11}}>—</span>
              <select value={newEH} onChange={e=>setNewEH(e.target.value)} style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 8px',fontSize:13,flex:1,outline:'none',fontFamily:'inherit'}}>
                {Array.from({length:12},(_,i)=>i+9).map(h=><option key={h} value={String(h).padStart(2,'0')}>{String(h).padStart(2,'0')}:00</option>)}
              </select>
            </div>
          </FancyInput>
        </div>
        <div style={{background:T.accentLight,border:`1px solid ${T.accentBorder}`,borderRadius:4,padding:'10px 12px',fontSize:12,color:T.accent,display:'flex',gap:8}}>
          <Ic n="info" size={13} color={T.accent}/>
          <span>改期将保留原申请编号，需重新走审批流程</span>
        </div>
        <div style={{display:'flex',gap:8,justifyContent:'flex-end'}}>
          <Btn variant="ghost" onClick={onClose}>取消</Btn>
          <Btn variant="orange" icon="rotate" onClick={()=>onConfirm(app,{newDate,newSH,newEH})}>确认改期</Btn>
        </div>
      </div>
    </Modal>
  );
};

// ── Mobile H5 View ────────────────────────────────────────────────
const MobileApp = ({ notify, apps, onNewApp }) => {
  const [tab, setTab] = useState('home');
  const TABS = [['home','首页','home'],['apply','申请','filePlus'],['mine','我的','list'],['notif','通知','bell']];

  const Home = () => (
    <div style={{flex:1,overflow:'auto',paddingBottom:56}}>
      <div style={{background:T.text1,padding:'24px 16px 28px',position:'relative',overflow:'hidden'}}>
        <div style={{position:'absolute',top:-20,right:-20,width:100,height:100,borderRadius:'50%',background:'rgba(255,86,0,0.15)'}}/>
        <div style={{position:'absolute',bottom:-30,right:30,width:60,height:60,borderRadius:'50%',background:'rgba(255,86,0,0.1)'}}/>
        <MonoLabel style={{color:'rgba(255,255,255,0.5)',display:'block',marginBottom:6}}>欢迎回来</MonoLabel>
        <div style={{fontSize:20,fontWeight:700,color:'#fff',marginBottom:4}}>李建国</div>
        <div style={{fontSize:12,color:'rgba(255,255,255,0.5)'}}>天河区 · 政企客户部</div>
        <div style={{display:'flex',gap:12,marginTop:20}}>
          {[['4','待处理申请','pending'],['1','今日参观','approved'],['2','待审批','warning']].map(([v,l,s])=>(
            <div key={l} style={{flex:1,background:'rgba(255,255,255,0.08)',borderRadius:6,padding:'10px 12px',border:'1px solid rgba(255,255,255,0.1)'}}>
              <div style={{fontSize:20,fontWeight:800,color:'#fff'}}>{v}</div>
              <div style={{fontSize:10,color:'rgba(255,255,255,0.5)',marginTop:2}}>{l}</div>
            </div>
          ))}
        </div>
      </div>
      <div style={{padding:'16px'}}>
        <div style={{display:'flex',gap:10,marginBottom:20}}>
          <button onClick={()=>setTab('apply')} style={{flex:1,background:T.accent,color:'#fff',border:'none',borderRadius:6,padding:'12px',fontSize:13,fontWeight:600,cursor:'pointer',display:'flex',alignItems:'center',justifyContent:'center',gap:6,fontFamily:'inherit'}}>
            <Ic n="plus" size={14} color="#fff"/>新建申请
          </button>
          <button onClick={()=>setTab('mine')} style={{flex:1,background:T.bg,color:T.text1,border:`1px solid ${T.border}`,borderRadius:6,padding:'12px',fontSize:13,fontWeight:600,cursor:'pointer',display:'flex',alignItems:'center',justifyContent:'center',gap:6,fontFamily:'inherit'}}>
            <Ic n="list" size={14}/>我的申请
          </button>
        </div>
        <MonoLabel style={{display:'block',marginBottom:10}}>最近申请</MonoLabel>
        <div style={{display:'flex',flexDirection:'column',gap:8}}>
          {apps.slice(0,3).map(a=>(
            <div key={a.id} style={{background:T.surface,border:`1px solid ${T.border}`,borderRadius:6,padding:'12px 14px'}}>
              <div style={{display:'flex',alignItems:'center',justifyContent:'space-between',marginBottom:5}}>
                <span style={{fontSize:13,fontWeight:600,color:T.text1,flex:1,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap',marginRight:8}}>{a.title}</span>
                <Badge status={a.status}/>
              </div>
              <span style={{fontSize:11,color:T.text3}}>{a.startTime.split(' ')[0]} · {a.unit}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );

  const MineList = () => (
    <div style={{flex:1,overflow:'auto',paddingBottom:56}}>
      <div style={{padding:'16px',borderBottom:`1px solid ${T.border}`}}>
        <div style={{fontSize:15,fontWeight:700,color:T.text1}}>我的申请</div>
        <div style={{fontSize:12,color:T.text3,marginTop:2}}>共 {apps.length} 条记录</div>
      </div>
      <div style={{padding:'12px 16px',display:'flex',flexDirection:'column',gap:8}}>
        {apps.map(a=>(
          <div key={a.id} style={{background:T.surface,border:`1px solid ${T.border}`,borderRadius:6,padding:'12px 14px'}}>
            <div style={{display:'flex',alignItems:'flex-start',justifyContent:'space-between',gap:8,marginBottom:6}}>
              <span style={{fontSize:13,fontWeight:600,color:T.text1,flex:1}}>{a.title}</span>
              <Badge status={a.status}/>
            </div>
            <div style={{display:'flex',flexDirection:'column',gap:3}}>
              {[[a.unit,'building'],[`${a.startTime.split(' ')[0]}  ${a.startTime.split(' ')[1]}–${a.endTime.split(' ')[1]}`,'calendar'],[`${a.headCount}人`,'users']].map(([t,ic])=>(
                <span key={t} style={{fontSize:11,color:T.text3,display:'flex',alignItems:'center',gap:4}}><Ic n={ic} size={11}/>{t}</span>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  const ApplyForm = () => (
    <div style={{flex:1,overflow:'auto',padding:'16px',paddingBottom:56}}>
      <div style={{fontSize:15,fontWeight:700,color:T.text1,marginBottom:16}}>新建参观申请</div>
      <div style={{display:'flex',flexDirection:'column',gap:12}}>
        {[['会议主题','text'],['来访单位','text'],['联系电话','tel']].map(([l,t])=>(
          <FancyInput key={l} label={l} value="" onChange={()=>{}} placeholder={`请输入${l}`} type={t} required/>
        ))}
        <FancyInput label="参观日期" required>
          <input type="date" style={{border:`1px solid ${T.border}`,borderRadius:4,padding:'8px 10px',fontSize:13,width:'100%',boxSizing:'border-box',outline:'none',fontFamily:'inherit'}}/>
        </FancyInput>
      </div>
      <div style={{marginTop:20}}>
        <Btn variant="orange" style={{width:'100%',justifyContent:'center',padding:'12px'}} onClick={()=>notify('请在桌面端完成完整申请流程','info')}>
          提交申请（完整功能请用桌面端）
        </Btn>
      </div>
    </div>
  );

  const content = tab==='home'?<Home/>:tab==='mine'?<MineList/>:tab==='apply'?<ApplyForm/>:(
    <div style={{flex:1,display:'flex',alignItems:'center',justifyContent:'center',padding:40,color:T.text3,flexDirection:'column',gap:8}}>
      <Ic n="bell" size={36} color={T.border}/>
      <span style={{fontSize:13}}>通知功能请查看桌面端</span>
    </div>
  );

  return (
    <div style={{display:'flex',flexDirection:'column',height:'100%',background:T.bg,position:'relative'}}>
      {/* Status bar */}
      <div style={{height:36,background:T.text1,display:'flex',alignItems:'center',justifyContent:'space-between',padding:'0 16px',flexShrink:0}}>
        <span style={{fontSize:12,fontWeight:700,color:'#fff',fontFamily:'monospace'}}>9:41</span>
        <div style={{display:'flex',gap:6,alignItems:'center'}}>
          {[3,3,3,3].map((_,i)=><div key={i} style={{width:3,height:3+i*2,background:'rgba(255,255,255,0.8)',borderRadius:1}}/>)}
          <div style={{width:24,height:11,border:'1.5px solid rgba(255,255,255,0.6)',borderRadius:2,padding:1,marginLeft:2}}>
            <div style={{width:'70%',height:'100%',background:'rgba(255,255,255,0.8)',borderRadius:1}}/>
          </div>
        </div>
      </div>
      {content}
      {/* Bottom nav */}
      <div style={{position:'absolute',bottom:0,left:0,right:0,height:56,background:T.surface,borderTop:`1px solid ${T.border}`,display:'flex',alignItems:'center'}}>
        {TABS.map(([k,l,ic])=>{
          const act=tab===k;
          return (
            <button key={k} onClick={()=>setTab(k)} style={{flex:1,border:'none',background:'none',cursor:'pointer',display:'flex',flexDirection:'column',alignItems:'center',gap:3,padding:'6px 0',fontFamily:'inherit'}}>
              <Ic n={ic} size={18} color={act?T.accent:T.text3}/>
              <span style={{fontSize:10,color:act?T.accent:T.text3,fontWeight:act?700:400}}>{l}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
};

// Phone frame wrapper
const PhoneFrame = ({ children }) => (
  <div style={{display:'flex',justifyContent:'center',padding:'20px 0'}}>
    <div style={{width:375,height:720,borderRadius:40,overflow:'hidden',border:'10px solid #111',boxShadow:'0 24px 60px rgba(17,17,17,0.3), inset 0 0 0 1px rgba(255,255,255,0.1)',background:T.text1,position:'relative',display:'flex',flexDirection:'column'}}>
      <div style={{position:'absolute',top:0,left:'50%',transform:'translateX(-50%)',width:100,height:26,background:T.text1,borderRadius:'0 0 16px 16px',zIndex:10}}/>
      <div style={{flex:1,overflow:'hidden'}}>{children}</div>
      <div style={{height:28,background:T.bg,display:'flex',alignItems:'center',justifyContent:'center',flexShrink:0}}>
        <div style={{width:100,height:4,background:'#ccc',borderRadius:2}}/>
      </div>
    </div>
  </div>
);

// ── Main Apply View ───────────────────────────────────────────────
const ApplyView = ({ notify, startOnNew, showMobile }) => {
  const [view, setView] = useState(startOnNew?'new':'list');
  const [apps, setApps] = useState(MOCK_APPLICATIONS);
  const [detailApp, setDetailApp] = useState(null);
  const [cancelApp, setCancelApp] = useState(null);
  const [rescheduleApp, setRescheduleApp] = useState(null);

  if(showMobile) return (
    <div>
      <div style={{marginBottom:16,display:'flex',alignItems:'center',gap:10}}>
        <div style={{display:'flex',gap:6,alignItems:'center',padding:'4px 10px',background:T.bg,border:`1px solid ${T.border}`,borderRadius:4}}>
          <Ic n="smartphone" size={12} color={T.accent}/><span style={{fontSize:12,color:T.accent,fontWeight:600}}>H5 移动端预览</span>
        </div>
        <span style={{fontSize:12,color:T.text3}}>申请人在手机上的实际体验</span>
      </div>
      <PhoneFrame><MobileApp notify={notify} apps={apps} onNewApp={()=>setView('new')}/></PhoneFrame>
    </div>
  );

  const handleSubmit=(form)=>{
    const newApp={id:`EH-2026-${String(Math.floor(Math.random()*9000)+1000)}`,title:form.title,unit:form.unit,industry:form.industry,district:form.district,applicant:form.applicant,phone:form.phone,dept:form.dept,startTime:`${form.startDate} ${form.startHour}:00`,endTime:`${form.startDate} ${form.endHour}:00`,leader:form.leader,visitors:form.visitors,services:form.services,status:'pending',approvalNodes:[{role:'部门领导',name:'待分配',action:'pending',time:null,comment:''},{role:'展厅主管',name:'刘主管',action:'waiting',time:null,comment:''},...(form.leader!=='无'?[{role:form.leader,name:'待分配',action:'waiting',time:null,comment:''}]:[])],headCount:parseInt(form.headCount)||0,agenda:form.agenda,created:new Date().toLocaleString('zh-CN'),opportunityCode:''};
    setApps(p=>[newApp,...p]); setView('list');
    notify(`申请「${form.title}」已提交，等待审批`,'success');
  };

  const handleCancel=(app,reason)=>{
    setApps(p=>p.map(a=>a.id===app.id?{...a,status:'cancelled'}:a));
    setCancelApp(null); notify(`申请「${app.title}」已取消`,'success');
  };

  const handleReschedule=(app,info)=>{
    setApps(p=>p.map(a=>a.id===app.id?{...a,status:'rescheduled',startTime:`${info.newDate} ${info.newSH}:00`,endTime:`${info.newDate} ${info.newEH}:00`}:a));
    setRescheduleApp(null); notify(`「${app.title}」改期成功，将重新审批`,'success');
  };

  const [filter,setFilter]=useState('all');
  const FILTERS=[['all','全部',apps.length],['pending','审批中',apps.filter(a=>a.status==='pending').length],['approved','已批准',apps.filter(a=>a.status==='approved').length],['completed','已完成',apps.filter(a=>a.status==='completed').length],['rejected','已驳回',apps.filter(a=>a.status==='rejected').length]];
  const shown=filter==='all'?apps:apps.filter(a=>a.status===filter);

  if(view==='new') return (
    <div>
      <div style={{display:'flex',alignItems:'center',gap:8,marginBottom:22}}>
        <button onClick={()=>setView('list')} style={{border:`1px solid ${T.border}`,background:T.surface,cursor:'pointer',borderRadius:4,padding:'6px 12px',display:'flex',alignItems:'center',gap:5,fontSize:12,color:T.text2,fontFamily:'inherit',fontWeight:500}}>
          <Ic n="arrowLeft" size={12}/>返回
        </button>
        <Ic n="chevronRight" size={12} color={T.text3}/>
        <span style={{fontSize:14,fontWeight:700,color:T.text1}}>新建参观申请</span>
      </div>
      <Card style={{padding:'24px 28px'}}>
        <NewAppForm onSubmit={handleSubmit} onCancel={()=>setView('list')} notify={notify}/>
      </Card>
    </div>
  );

  return (
    <>
      <div style={{display:'flex',alignItems:'flex-start',justifyContent:'space-between',marginBottom:18}}>
        <div><h2 style={{margin:0,fontSize:19,fontWeight:700,color:T.text1}}>我的申请</h2><p style={{margin:'3px 0 0',fontSize:12,color:T.text3}}>共 {apps.length} 条申请记录</p></div>
        <Btn variant="primary" icon="plus" onClick={()=>setView('new')}>新建申请</Btn>
      </div>
      <div style={{display:'flex',gap:6,marginBottom:16,flexWrap:'wrap'}}>
        {FILTERS.map(([k,l,cnt])=>(
          <button key={k} onClick={()=>setFilter(k)} style={{border:`1px solid ${filter===k?T.text1:T.border}`,background:filter===k?T.text1:T.surface,color:filter===k?'#fff':T.text2,padding:'5px 12px',borderRadius:4,cursor:'pointer',fontSize:12,fontWeight:600,display:'flex',alignItems:'center',gap:5,transition:'all 0.12s',fontFamily:'inherit'}}>
            {l}<span style={{background:filter===k?'rgba(255,255,255,0.2)':'#f0ede8',color:filter===k?'#fff':T.text3,borderRadius:3,padding:'0px 5px',fontSize:11}}>{cnt}</span>
          </button>
        ))}
      </div>
      {shown.length===0?(
        <div style={{textAlign:'center',padding:'64px 0',color:T.text3}}>
          <div style={{display:'flex',justifyContent:'center',marginBottom:14,opacity:0.25}}><Ic n="clipboard" size={44} color={T.text2}/></div>
          <p style={{fontSize:13,margin:'0 0 14px'}}>暂无相关申请</p>
          <Btn variant="outline" icon="plus" onClick={()=>setView('new')}>立即新建</Btn>
        </div>
      ):(
        <div style={{display:'flex',flexDirection:'column',gap:8}}>
          {shown.map(app=>(
            <Card key={app.id} onClick={()=>setDetailApp(app)} style={{padding:'16px 18px'}}>
              <div style={{display:'flex',alignItems:'flex-start',gap:12}}>
                <div style={{width:36,height:36,borderRadius:6,background:T.bg,border:`1px solid ${T.border}`,display:'flex',alignItems:'center',justifyContent:'center',flexShrink:0,marginTop:2}}>
                  <Ic n="building" size={16} color={T.text2}/>
                </div>
                <div style={{flex:1,minWidth:0}}>
                  <div style={{display:'flex',alignItems:'center',gap:8,flexWrap:'wrap',marginBottom:5}}>
                    <span style={{fontSize:14,fontWeight:700,color:T.text1}}>{app.title}</span>
                    <Badge status={app.status}/>
                  </div>
                  <div style={{display:'flex',gap:14,flexWrap:'wrap'}}>
                    {[[app.unit,'building'],[`${app.startTime.split(' ')[0]}  ${app.startTime.split(' ')[1]}–${app.endTime.split(' ')[1]}`,'calendar'],[`${app.headCount}人`,'users'],[app.id,'clipboard']].map(([text,ic])=>(
                      <span key={text} style={{display:'inline-flex',alignItems:'center',gap:4,fontSize:11,color:T.text3}}><Ic n={ic} size={11}/>{text}</span>
                    ))}
                  </div>
                  {app.status==='pending'&&(
                    <div style={{display:'flex',gap:3,alignItems:'center',marginTop:7}}>
                      {app.approvalNodes.map((n,i)=>(
                        <React.Fragment key={i}>
                          <span style={{fontSize:10,padding:'2px 6px',borderRadius:3,background:n.action==='approved'?T.successLight:n.action==='pending'?T.warningLight:T.bg,color:n.action==='approved'?T.success:n.action==='pending'?T.warning:T.text3,fontWeight:600,border:`1px solid ${n.action==='approved'?T.success+'33':n.action==='pending'?T.warning+'33':T.border}`}}>{n.role}</span>
                          {i<app.approvalNodes.length-1&&<Ic n="chevronRight" size={9} color={T.text3}/>}
                        </React.Fragment>
                      ))}
                    </div>
                  )}
                </div>
                <Ic n="chevronRight" size={14} color={T.text3}/>
              </div>
            </Card>
          ))}
        </div>
      )}
      <AppDetail app={detailApp} onClose={()=>setDetailApp(null)} onCancel={a=>{setDetailApp(null);setCancelApp(a);}} onReschedule={a=>{setDetailApp(null);setRescheduleApp(a);}}/>
      <CancelModal app={cancelApp} onClose={()=>setCancelApp(null)} onConfirm={handleCancel}/>
      <RescheduleModal app={rescheduleApp} onClose={()=>setRescheduleApp(null)} onConfirm={handleReschedule}/>
    </>
  );
};

Object.assign(window, { ApplyView });
