
import React from 'react';
import {Activity,LayoutDashboard,Boxes,Lightbulb,Settings2,ChevronRight,ShieldCheck,Package} from 'lucide-react';
const items=[{id:'overview',icon:LayoutDashboard,label:'Overview'},{id:'inventory',icon:Boxes,label:'Inventory'},{id:'decisions',icon:Lightbulb,label:'Recommendations'}];
export default function Sidebar({active,onNavigate,pending}){
 return <aside className="sidebar">
  <div className="logo"><div className="logo-symbol"><Activity size={22} strokeWidth={2.7}/></div><div><strong>stock<span>pulse</span></strong><small>COMMERCE OS</small></div></div>
  <div className="nav-caption">WORKSPACE</div>
  <nav>{items.map(({id,icon:Icon,label})=><button key={id} onClick={()=>onNavigate(id)} className={'nav-item '+(active===id?'active':'')}><Icon size={18}/><span>{label}</span>{id==='decisions'&&pending>0?<em>{pending}</em>:null}{active===id&&<ChevronRight size={15} className="nav-chevron"/>}</button>)}</nav>
  <div className="nav-caption second-caption">SYSTEM</div>
  <div className="nav-static"><Settings2 size={17}/> Configuration</div>
  <div className="sidebar-bottom"><div className="side-status"><div className="status-icon"><ShieldCheck size={19}/></div><div><b>Human-in-the-loop</b><small>Approval safeguards active</small></div></div><div className="side-profile"><div className="avatar">SP</div><div><b>Store Admin</b><small>Operations workspace</small></div><Package size={16}/></div></div>
 </aside>
}
