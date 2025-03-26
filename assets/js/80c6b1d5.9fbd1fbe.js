"use strict";(self.webpackChunkrosetta_docs=self.webpackChunkrosetta_docs||[]).push([[186],{2765:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>d,contentTitle:()=>t,default:()=>h,frontMatter:()=>a,metadata:()=>r,toc:()=>c});const r=JSON.parse('{"id":"advanced/configuration-and-performance","title":"Advanced Configuration and Performance","description":"Advanced configuration options and performance tuning","source":"@site/docs/advanced/configuration-and-performance.md","sourceDirName":"advanced","slug":"/advanced/configuration-and-performance","permalink":"/cardano-rosetta-java/docs/advanced/configuration-and-performance","draft":false,"unlisted":false,"editUrl":"https://github.com/cardano-foundation/cardano-rosetta-java/tree/main/docs/docs/advanced/configuration-and-performance.md","tags":[],"version":"current","sidebarPosition":1,"frontMatter":{"sidebar_position":1,"title":"Advanced Configuration and Performance","description":"Advanced configuration options and performance tuning"},"sidebar":"tutorialSidebar","previous":{"title":"Advanced Configuration and Performance","permalink":"/cardano-rosetta-java/docs/category/advanced-configuration-and-performance"},"next":{"title":"Contributing","permalink":"/cardano-rosetta-java/docs/category/contributing"}}');var o=i(4848),s=i(8453);const a={sidebar_position:1,title:"Advanced Configuration and Performance",description:"Advanced configuration options and performance tuning"},t="Advanced Configuration and Performance",d={},c=[{value:"1. Pruning (Disk Usage Optimization)",id:"1-pruning-disk-usage-optimization",level:2},{value:"1.1. When to Enable Pruning",id:"11-when-to-enable-pruning",level:3},{value:"1.2. When to Avoid Pruning",id:"12-when-to-avoid-pruning",level:3},{value:"2. Database Pool Settings (HikariCP)",id:"2-database-pool-settings-hikaricp",level:2},{value:"2.1. Example",id:"21-example",level:3},{value:"2.2 When to Increase Pool Size",id:"22-when-to-increase-pool-size",level:3},{value:"3. Tomcat Thread Configuration",id:"3-tomcat-thread-configuration",level:2},{value:"4. Example <code>.env</code> Settings",id:"4-example-env-settings",level:2},{value:"Further Reading",id:"further-reading",level:2}];function l(e){const n={a:"a",code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",hr:"hr",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",ul:"ul",...(0,s.R)(),...e.components};return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsx)(n.header,{children:(0,o.jsx)(n.h1,{id:"advanced-configuration-and-performance",children:"Advanced Configuration and Performance"})}),"\n",(0,o.jsxs)(n.p,{children:["This guide provides details on how to tune ",(0,o.jsx)(n.strong,{children:"cardano-rosetta-java"})," for various workloads and resource constraints. It covers:"]}),"\n",(0,o.jsxs)(n.ol,{children:["\n",(0,o.jsx)(n.li,{children:(0,o.jsx)(n.a,{href:"#1-pruning-disk-usage-optimization",children:"Pruning (Disk Usage Optimization)"})}),"\n",(0,o.jsx)(n.li,{children:(0,o.jsx)(n.a,{href:"#2-database-pool-settings-hikaricp",children:"Database Pool Settings (HikariCP)"})}),"\n",(0,o.jsx)(n.li,{children:(0,o.jsx)(n.a,{href:"#3-tomcat-thread-configuration",children:"Tomcat Thread Configuration"})}),"\n",(0,o.jsx)(n.li,{children:(0,o.jsxs)(n.a,{href:"#4-example-env-settings",children:["Example ",(0,o.jsx)(n.code,{children:".env"})," Settings"]})}),"\n"]}),"\n",(0,o.jsx)(n.hr,{}),"\n",(0,o.jsx)(n.h2,{id:"1-pruning-disk-usage-optimization",children:"1. Pruning (Disk Usage Optimization)"}),"\n",(0,o.jsx)(n.p,{children:"Pruning removes spent (consumed) UTXOs from local storage, keeping only unspent UTXOs. This can reduce on-disk storage from ~1TB down to ~400GB, but discards historical transaction data."}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsx)(n.li,{children:"Only unspent outputs are preserved."}),"\n",(0,o.jsx)(n.li,{children:"You can still validate the chain\u2019s current state (and spend tokens), since active UTXOs remain."}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Enable Pruning"}),": Set ",(0,o.jsx)(n.code,{children:"PRUNING_ENABLED=true"})," in your environment (e.g., in ",(0,o.jsx)(n.code,{children:".env.dockerfile"})," or ",(0,o.jsx)(n.code,{children:".env.docker-compose"}),")."]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Disable Pruning"})," (default): Set ",(0,o.jsx)(n.code,{children:"PRUNING_ENABLED=false"}),"."]}),"\n"]}),"\n",(0,o.jsx)(n.h3,{id:"11-when-to-enable-pruning",children:"1.1. When to Enable Pruning"}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Low Disk Environments"}),": If you need to minimize disk usage and only require UTXO data for current balances."]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Exploratory / Dev Environments"}),": If historical queries are not critical."]}),"\n"]}),"\n",(0,o.jsx)(n.h3,{id:"12-when-to-avoid-pruning",children:"1.2. When to Avoid Pruning"}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Full Historical Data Requirements"}),": If you need the complete transaction history\u2014whether for exchange operations, audit trails, or compliance mandates\u2014do not enable pruning. Pruning discards spent UTXOs, which removes older transaction data and prevents certain types of historical lookups or reporting."]}),"\n"]}),"\n",(0,o.jsx)(n.hr,{}),"\n",(0,o.jsx)(n.h2,{id:"2-database-pool-settings-hikaricp",children:"2. Database Pool Settings (HikariCP)"}),"\n",(0,o.jsxs)(n.p,{children:["cardano-rosetta-java uses ",(0,o.jsx)(n.a,{href:"https://github.com/brettwooldridge/HikariCP",children:"HikariCP"})," as the JDBC connection pool. Tuning these values can help manage concurrency and performance."]}),"\n",(0,o.jsxs)(n.table,{children:[(0,o.jsx)(n.thead,{children:(0,o.jsxs)(n.tr,{children:[(0,o.jsx)(n.th,{children:"Variable"}),(0,o.jsx)(n.th,{children:"Purpose"}),(0,o.jsx)(n.th,{children:"Common Defaults"}),(0,o.jsx)(n.th,{children:"Possible Tuning"})]})}),(0,o.jsxs)(n.tbody,{children:[(0,o.jsxs)(n.tr,{children:[(0,o.jsx)(n.td,{children:(0,o.jsx)(n.code,{children:"SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE"})}),(0,o.jsx)(n.td,{children:"Max number of DB connections in the pool"}),(0,o.jsx)(n.td,{children:"10 (example)"}),(0,o.jsx)(n.td,{children:"20\u2013100"})]}),(0,o.jsxs)(n.tr,{children:[(0,o.jsx)(n.td,{children:(0,o.jsx)(n.code,{children:"SPRING_DATASOURCE_HIKARI_LEAKDETECTIONTHRESHOLD"})}),(0,o.jsx)(n.td,{children:"Time (ms) before a connection leak warning is logged"}),(0,o.jsx)(n.td,{children:"30,000"}),(0,o.jsx)(n.td,{children:"300,000"})]}),(0,o.jsxs)(n.tr,{children:[(0,o.jsx)(n.td,{children:(0,o.jsx)(n.code,{children:"SPRING_DATASOURCE_HIKARI_CONNECTIONTIMEOUT"})}),(0,o.jsx)(n.td,{children:"Max time (ms) to wait for a free connection before error"}),(0,o.jsx)(n.td,{children:"30,000"}),(0,o.jsx)(n.td,{children:"300,000"})]})]})]}),"\n",(0,o.jsx)(n.h3,{id:"21-example",children:"2.1. Example"}),"\n",(0,o.jsx)(n.p,{children:"If you\u2019re dealing with high API request volume, consider:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-bash",children:"SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=50\nSPRING_DATASOURCE_HIKARI_LEAKDETECTIONTHRESHOLD=300000\nSPRING_DATASOURCE_HIKARI_CONNECTIONTIMEOUT=300000\n"})}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsx)(n.li,{children:"This allows up to 50 connections in the pool."}),"\n",(0,o.jsx)(n.li,{children:"The large leak detection threshold (5 minutes) can help in debugging slow queries."}),"\n"]}),"\n",(0,o.jsx)(n.h3,{id:"22-when-to-increase-pool-size",children:"2.2 When to Increase Pool Size"}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsx)(n.li,{children:"If your logs show \u201cconnection timeout\u201d or \u201cpool is exhausted,\u201d your current pool size may be insufficient."}),"\n",(0,o.jsxs)(n.li,{children:["Only increase ",(0,o.jsx)(n.code,{children:"SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE"})," if your database has the resources (CPU, RAM, I/O) to handle additional connections."]}),"\n"]}),"\n",(0,o.jsx)(n.hr,{}),"\n",(0,o.jsx)(n.h2,{id:"3-tomcat-thread-configuration",children:"3. Tomcat Thread Configuration"}),"\n",(0,o.jsxs)(n.p,{children:["By default, Spring Boot (Tomcat) handles incoming HTTP requests with a thread pool. If you anticipate ",(0,o.jsx)(n.strong,{children:"very high"})," concurrency, you might adjust:"]}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:(0,o.jsx)(n.code,{children:"SERVER_TOMCAT_THREADS_MAX"})}),": Maximum number of threads Tomcat uses to handle requests.","\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsxs)(n.li,{children:["Start with the default (",(0,o.jsx)(n.code,{children:"200"}),")."]}),"\n",(0,o.jsx)(n.li,{children:"Increasing this limit can help in high-concurrency scenarios, but if your system\u2019s bottleneck is elsewhere (e.g., database, network, or CPU), you may see limited performance gains."}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Only"})," increase if profiling shows that Tomcat threads are maxed out and your DB can keep up."]}),"\n",(0,o.jsx)(n.li,{children:"Check CPU/memory usage carefully; going too high can lead to contention and slowdowns."}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,o.jsx)(n.hr,{}),"\n",(0,o.jsxs)(n.h2,{id:"4-example-env-settings",children:["4. Example ",(0,o.jsx)(n.code,{children:".env"})," Settings"]}),"\n",(0,o.jsxs)(n.p,{children:["Below is a snippet of how you might configure ",(0,o.jsx)(n.code,{children:".env.dockerfile"})," or ",(0,o.jsx)(n.code,{children:".env.docker-compose"})," for higher throughput:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-bash",children:"# --- Pruning Toggle ---\nPRUNING_ENABLED=false\n# Keep full history, requires ~1TB of disk space\n\n# --- HikariCP Database Pool ---\nSPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=50\nSPRING_DATASOURCE_HIKARI_LEAKDETECTIONTHRESHOLD=300000\nSPRING_DATASOURCE_HIKARI_CONNECTIONTIMEOUT=300000\n\n# --- Tomcat Thread Pool ---\n# SERVER_TOMCAT_THREADS_MAX=200\n# Uncomment and set a higher value if needed:\n# SERVER_TOMCAT_THREADS_MAX=400\n"})}),"\n",(0,o.jsx)(n.hr,{}),"\n",(0,o.jsx)(n.h2,{id:"further-reading",children:"Further Reading"}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsx)(n.li,{children:(0,o.jsx)(n.a,{href:"https://docs.cdp.coinbase.com/mesh/docs/api-reference/",children:"Rosetta API Reference"})}),"\n",(0,o.jsx)(n.li,{children:(0,o.jsx)(n.a,{href:"https://github.com/bloxbean/yaci-store",children:"Yaci-Store Repository"})}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.a,{href:"https://docs.spring.io/spring-boot/index.html",children:"Spring Boot Docs"})," (for more advanced server and DB config)"]}),"\n"]})]})}function h(e={}){const{wrapper:n}={...(0,s.R)(),...e.components};return n?(0,o.jsx)(n,{...e,children:(0,o.jsx)(l,{...e})}):l(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>a,x:()=>t});var r=i(6540);const o={},s=r.createContext(o);function a(e){const n=r.useContext(s);return r.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function t(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:a(e.components),r.createElement(s.Provider,{value:n},e.children)}}}]);