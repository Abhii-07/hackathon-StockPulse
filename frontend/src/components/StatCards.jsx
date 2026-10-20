
import React from 'react';
import {Package,AlertTriangle,TrendingUp,ClipboardList,ArrowUpRight} from 'lucide-react';
export default function StatCards({products,pricing,reorders}){
 const low=products.filter(p=>p.stockLevel<p.reorderThreshold).length;
 const cards=[
 {label:'Total products',value:products.length,sub:'SKUs being monitored',icon:Package,kind:'violet'},
 {label:'Low stock alerts',value:low,sub:'Need your attention',icon:AlertTriangle,kind:'orange'},
 {label:'Pricing suggestions',value:pricing.length,sub:'Awaiting approval',icon:TrendingUp,kind:'blue'},
 {label:'Reorder requests',value:reorders.length,sub:'Awaiting approval',icon:ClipboardList,kind:'mint'}];
 return <div className="stats-grid">{cards.map(({label,value,sub,icon:Icon,kind})=><div className="stat-card" key={label}><div className="stat-top"><div className={'stat-icon '+kind}><Icon size={20}/></div><ArrowUpRight size={17} className="stat-arrow"/></div><span className="stat-label">{label}</span><strong>{value.toString().padStart(2,'0')}</strong><small>{sub}</small></div>)}</div>
}
