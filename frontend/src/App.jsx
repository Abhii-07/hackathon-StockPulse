
import React,{useState} from 'react';
import {ArrowRight,Activity,AlertCircle,CheckCircle2,RefreshCw,CalendarDays} from 'lucide-react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import StatCards from './components/StatCards';
import AdvisorPanel from './components/AdvisorPanel';
import InventoryTable from './components/InventoryTable';
import DecisionQueue from './components/DecisionQueue';
import AddProductModal from './components/AddProductModal';
import {useDashboard} from './hooks/useDashboard';
export default function App(){
 const data=useDashboard();
 const [active,setActive]=useState('overview');
 const [search,setSearch]=useState('');
 const [showAdd,setShowAdd]=useState(false);
 const navigate=id=>{setActive(id);document.getElementById(id)?.scrollIntoView({behavior:'smooth',block:'start'})};
 const pending=data.pricing.length+data.reorders.length;
 return <div className="app-shell"><Sidebar active={active} onNavigate={navigate} pending={pending}/><div className="main-shell"><Header refresh={()=>data.refresh()} updatedAt={data.updatedAt} busy={data.busy} search={search} setSearch={setSearch} pending={pending}/><main className="content" id="overview"><div className="welcome-row"><div><div className="welcome-kicker"><span className="welcome-dot"/> LIVE OPERATIONS DASHBOARD</div><h1>Good to see you <span className="wave">✳</span></h1><p>Here's what's happening across your commerce operations today.</p></div><div className="date-card"><CalendarDays size={17}/>{new Date().toLocaleDateString('en-US',{weekday:'short',month:'short',day:'numeric',year:'numeric'})}</div></div>
 {data.error&&<div className="alert error"><AlertCircle size={17}/><span>{data.error}</span><button onClick={()=>data.setError('')}>×</button></div>}
 {data.toast&&<div className="alert success"><CheckCircle2 size={17}/><span>{data.toast}</span><button onClick={()=>data.setToast('')}>×</button></div>}
 <StatCards products={data.products} pricing={data.pricing} reorders={data.reorders}/>
 <div className="insights-row"><AdvisorPanel mode={data.mode} run={data.run} busy={data.busy} scrollToDecisions={()=>navigate('decisions')}/><div className="activity-panel"><div className="activity-title"><div className="activity-icon"><Activity size={19}/></div><div><b>System pulse</b><span>Real-time monitoring</span></div><span className="live-badge"><i/> LIVE</span></div><div className="activity-body"><div className="activity-line"><span className="activity-marker mint"/><div><b>Inventory monitoring</b><small>{data.products.length} products under observation</small></div><span>Active</span></div><div className="activity-line"><span className="activity-marker amber"/><div><b>Pending decisions</b><small>{pending} suggestions awaiting approval</small></div><span>{pending} queued</span></div><div className="activity-line"><span className="activity-marker violet"/><div><b>Recommendation engine</b><small>{data.mode==='AI'?'Gemini AI with safe fallback':'Deterministic rule strategy'}</small></div><span>{data.mode}</span></div></div><div className="activity-bottom"><RefreshCw size={13}/> Auto-sync every 4 seconds <span>{data.updatedAt?`Updated ${data.updatedAt.toLocaleTimeString()}`:'Connecting...'}</span></div></div></div>
 <InventoryTable products={data.products} search={search} run={data.run} busy={data.busy} onAdd={()=>setShowAdd(true)}/>
 <DecisionQueue pricing={data.pricing} reorders={data.reorders} products={data.products} run={data.run} busy={data.busy}/>
 <footer className="app-footer"><span><Activity size={16}/> STOCKPULSE</span><span>Intelligent commerce. Human decisions.</span><span>© 2026 StockPulse</span></footer>
 </main></div>{showAdd&&<AddProductModal close={()=>setShowAdd(false)} run={data.run} busy={data.busy}/>}</div>
}
