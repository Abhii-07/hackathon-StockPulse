
import {useCallback,useEffect,useRef,useState} from 'react';
import {api} from '../lib/api';
export function useDashboard(){
  const [products,setProducts]=useState([]);
  const [pricing,setPricing]=useState([]);
  const [reorders,setReorders]=useState([]);
  const [mode,setMode]=useState('RULE');
  const [loading,setLoading]=useState(true);
  const [busy,setBusy]=useState(false);
  const [error,setError]=useState('');
  const [toast,setToast]=useState('');
  const [updatedAt,setUpdatedAt]=useState(null);
  const refreshing=useRef(false);
  const refresh=useCallback(async({silent=false}={})=>{
    if(refreshing.current)return;
    refreshing.current=true;
    try{
      const [p,ps,rs,m]=await Promise.allSettled([api.products(),api.pricing(),api.reorders(),api.mode()]);
      if(p.status==='rejected')throw p.reason;
      setProducts(Array.isArray(p.value)?p.value:[]);
      if(ps.status==='fulfilled')setPricing(Array.isArray(ps.value)?ps.value:[]);
      if(rs.status==='fulfilled')setReorders(Array.isArray(rs.value)?rs.value:[]);
      if(m.status==='fulfilled')setMode(m.value?.mode||'RULE');
      const secondary=[ps,rs,m].filter(x=>x.status==='rejected');
      if(secondary.length&&!silent)setError('Some dashboard endpoints are unavailable. Verify backend approval and advisor routes.');
      else setError('');
      setUpdatedAt(new Date());
    }catch(e){if(!silent)setError(e.message);}
    finally{refreshing.current=false;setLoading(false);}
  },[]);
  useEffect(()=>{refresh();const t=setInterval(()=>refresh({silent:true}),4000);return()=>clearInterval(t)},[refresh]);
  const run=useCallback(async(action,success)=>{
    setBusy(true);setError('');setToast('');
    try{await action();setToast(success||'Action completed');await refresh();}
    catch(e){setError(e.message)}
    finally{setBusy(false)}
  },[refresh]);
  return {products,pricing,reorders,mode,loading,busy,error,toast,updatedAt,refresh,run,setError,setToast};
}
