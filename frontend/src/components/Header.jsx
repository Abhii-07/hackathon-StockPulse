
import React from 'react';
import {Bell,RefreshCw,Search,Command} from 'lucide-react';
export default function Header({refresh,updatedAt,busy,search,setSearch,pending}){
 return <header className="topbar"><div className="breadcrumb"><span>Workspace</span><span className="crumb-divider">/</span><b>Overview</b></div>
 <div className="top-actions"><div className="search-field"><Search size={16}/><input aria-label="Search inventory" value={search} onChange={e=>setSearch(e.target.value)} placeholder="Search products or SKU"/><kbd><Command size={11}/> K</kbd></div>
 <button className="icon-button" title="Refresh data" onClick={refresh} disabled={busy}><RefreshCw size={17}/></button><div className="topbar-notification"><Bell size={18}/>{pending>0&&<i/>}</div><div className="top-avatar">SP</div></div></header>
}
