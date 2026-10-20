
import React from 'react';
import {BrainCircuit,ShieldCheck,Sparkles,CheckCircle2,ArrowRight} from 'lucide-react';
import {api} from '../lib/api';
export default function AdvisorPanel({mode,run,busy,scrollToDecisions}){
 return <section className="advisor-panel"><div className="advisor-glow"/><div className="advisor-content"><div className="advisor-kicker"><Sparkles size={14}/> INTELLIGENCE ENGINE</div><div className="advisor-headline"><BrainCircuit size={28}/><h2>Smarter decisions.<br/><span>Better inventory.</span></h2></div><p>Choose between predictable business rules and Gemini-powered commerce recommendations.</p><div className="mode-toggle"><button className={mode==='RULE'?'chosen':''} disabled={busy} onClick={()=>run(()=>api.setMode('RULE'),'Rule engine activated')}><ShieldCheck size={16}/> Rule engine</button><button className={mode==='AI'?'chosen':''} disabled={busy} onClick={()=>run(()=>api.setMode('AI'),'Gemini AI activated')}><Sparkles size={16}/> Gemini AI</button></div><div className="advisor-footer"><span><CheckCircle2 size={15}/> {mode==='AI'?'AI active · rule fallback enabled':'Deterministic strategy active'}</span><button onClick={scrollToDecisions}>View queue <ArrowRight size={15}/></button></div></div></section>
}
