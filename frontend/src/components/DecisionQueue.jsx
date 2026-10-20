
import React,{useState} from 'react';
import {ArrowRight,Check,CheckCircle2,X,Clock3,TrendingUp,PackagePlus,Sparkles,Inbox,Info} from 'lucide-react';
import {api} from '../lib/api';
const confidence=v=>`${Math.round(Number(v||0)<=1?Number(v||0)*100:Number(v||0))}%`;
function SuggestionCard({item,type,products,run,busy}){
 const pricing=type==='pricing';
 const product=products.find(p=>String(p.id)===String(item.productId));
 const [expanded,setExpanded]=useState(false);
 const name=product?.name||`Product #${item.productId}`;
 return <article className="decision-card"><div className="decision-top"><div className={'decision-symbol '+(pricing?'pricing':'reorder')}>{pricing?<TrendingUp size={18}/>:<PackagePlus size={18}/>}</div><div className="decision-name"><strong>{name}</strong><small>{product?.sku||'INVENTORY SIGNAL'}</small></div><span className={'trigger '+(item.triggerReason==='MANUAL'?'manual':'automatic')}>{item.triggerReason?.replaceAll('_',' ')||'MANUAL'}</span></div>
 <div className="recommendation-figure"><span>{pricing?'Recommended price':'Suggested replenishment'}</span><div>{pricing?<><b>${Number(item.currentPrice||product?.currentPrice||0).toFixed(2)}</b><ArrowRight size={19}/><strong>${Number(item.recommendedPrice||0).toFixed(2)}</strong></>:<><strong>+{item.recommendedQuantity||0}</strong><em>units</em></>}</div></div>
 <div className="confidence-row"><div><Sparkles size={14}/> AI / rule reasoning</div><span>{confidence(item.confidence)} confidence</span></div><div className="confidence-track"><i style={{width:confidence(item.confidence)}}/></div>
 <p className={'reasoning '+(!expanded?'clamp':'')}>{item.reasoning||'Recommendation generated based on current inventory and demand signals.'}</p><button className="read-reasoning" onClick={()=>setExpanded(!expanded)}>{expanded?'Show less':'View reasoning'} <Info size={13}/></button>
 <div className="decision-actions"><button className="approve" disabled={busy} onClick={()=>run(()=>pricing?api.decidePrice(item.id,'ACCEPTED'):api.decideReorder(item.id,'ACCEPTED'),`${pricing?'Pricing':'Reorder'} suggestion approved`)}><Check size={16}/> Approve</button><button className="reject" disabled={busy} onClick={()=>run(()=>pricing?api.decidePrice(item.id,'REJECTED'):api.decideReorder(item.id,'REJECTED'),'Suggestion rejected')}><X size={15}/> Reject</button></div></article>
}
export default function DecisionQueue({pricing,reorders,products,run,busy}){
 const [tab,setTab]=useState('all');
 const all=[...pricing.map(s=>({...s,_type:'pricing'})),...reorders.map(s=>({...s,_type:'reorder'}))];
 const items=tab==='all'?all:all.filter(s=>s._type===tab);
 return <section className="panel queue-panel" id="decisions"><div className="panel-header"><div><div className="section-eyebrow">HUMAN-IN-THE-LOOP</div><h2>Decision center <span className="count-tag">{all.length}</span></h2><p>Review AI-generated actions before they affect live inventory.</p></div><div className="pending-indicator"><Clock3 size={15}/>{all.length} awaiting review</div></div>
 <div className="queue-tabs"><button className={tab==='all'?'active':''} onClick={()=>setTab('all')}>All recommendations <span>{all.length}</span></button><button className={tab==='pricing'?'active':''} onClick={()=>setTab('pricing')}>Pricing <span>{pricing.length}</span></button><button className={tab==='reorder'?'active':''} onClick={()=>setTab('reorder')}>Replenishment <span>{reorders.length}</span></button></div>
 {items.length?<div className="decisions-grid">{items.map(s=><SuggestionCard key={`${s._type}-${s.id}`} item={s} type={s._type} products={products} run={run} busy={busy}/>)}</div>:<div className="empty-decisions"><div><Inbox size={31}/></div><h3>You're all caught up</h3><p>When inventory drops or demand spikes, new recommendations will appear here automatically.</p><span><CheckCircle2 size={15}/> All decisions reviewed</span></div>}
 </section>
}
