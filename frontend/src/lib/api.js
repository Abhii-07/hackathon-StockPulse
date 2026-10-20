
const ROOT='/api';
async function request(path,{method='GET',body,...rest}={}){
  let response;
  try {
    response=await fetch(ROOT+path,{method,headers:{'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body),...rest});
  }catch{throw new Error('Backend unavailable. Start Spring Boot on port 8080.')}
  const contentType=response.headers.get('content-type')||'';
  const data=contentType.includes('application/json')?await response.json().catch(()=>null):await response.text().catch(()=>null);
  if(!response.ok)throw new Error(data?.message||data?.detail||data?.error||`Request failed (${response.status})`);
  return data;
}
export const api={
  products:()=>request('/products'),
  createProduct:(data)=>request('/products',{method:'POST',body:data}),
  sale:(id,quantity)=>request(`/products/${id}/orders`,{method:'POST',body:{quantity}}),
  updateStock:(id,stockLevel)=>request(`/products/${id}/stock`,{method:'PATCH',body:{stockLevel}}),
  pricing:()=>request('/pricing-suggestions?status=PENDING'),
  reorders:()=>request('/reorder-suggestions?status=PENDING'),
  suggestPrice:(id)=>request(`/products/${id}/suggest-pricing`,{method:'POST'}),
  suggestReorder:(id)=>request(`/products/${id}/suggest-reorder`,{method:'POST'}),
  decidePrice:(id,status)=>request(`/pricing-suggestions/${id}`,{method:'PATCH',body:{status}}),
  decideReorder:(id,status)=>request(`/reorder-suggestions/${id}`,{method:'PATCH',body:{status}}),
  mode:()=>request('/advisor/mode'),
  setMode:(mode)=>request('/advisor/mode',{method:'POST',body:{mode}})
};
