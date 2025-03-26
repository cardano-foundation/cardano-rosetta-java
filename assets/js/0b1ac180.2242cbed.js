"use strict";(self.webpackChunkrosetta_docs=self.webpackChunkrosetta_docs||[]).push([[863],{3030:(e,t,a)=>{a.d(t,{A:()=>o});const o=a.p+"assets/images/ContextDiagram.drawio-8ad497d97881e668a3002b12794f93dc.svg"},8453:(e,t,a)=>{a.d(t,{R:()=>r,x:()=>s});var o=a(6540);const n={},i=o.createContext(n);function r(e){const t=o.useContext(i);return o.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function s(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(n):e.components||n:r(e.components),o.createElement(i.Provider,{value:t},e.children)}},8514:(e,t,a)=>{a.d(t,{A:()=>o});const o=a.p+"assets/images/ComponentDiagram.drawio-cca5233e69801b51dd3e3c58ad1c0574.svg"},9259:(e,t,a)=>{a.r(t),a.d(t,{assets:()=>c,contentTitle:()=>s,default:()=>l,frontMatter:()=>r,metadata:()=>o,toc:()=>d});const o=JSON.parse('{"id":"architecture/overview","title":"Architecture Overview","description":"Overview of Cardano Rosetta Java architecture","source":"@site/docs/architecture/overview.md","sourceDirName":"architecture","slug":"/architecture/overview","permalink":"/cardano-rosetta-java/docs/architecture/overview","draft":false,"unlisted":false,"editUrl":"https://github.com/cardano-foundation/cardano-rosetta-java/tree/main/docs/docs/architecture/overview.md","tags":[],"version":"current","sidebarPosition":1,"frontMatter":{"sidebar_position":1,"title":"Architecture Overview","description":"Overview of Cardano Rosetta Java architecture"},"sidebar":"tutorialSidebar","previous":{"title":"Architecture","permalink":"/cardano-rosetta-java/docs/category/architecture"},"next":{"title":"Cardano Specific API Additions","permalink":"/cardano-rosetta-java/docs/category/cardano-specific-api-additions"}}');var n=a(4848),i=a(8453);const r={sidebar_position:1,title:"Architecture Overview",description:"Overview of Cardano Rosetta Java architecture"},s="Architecture Overview",c={},d=[{value:"Context",id:"context",level:2},{value:"Implementation",id:"implementation",level:2},{value:"Cardano Node",id:"cardano-node",level:3},{value:"Yaci Indexer App",id:"yaci-indexer-app",level:3},{value:"Rosetta API App",id:"rosetta-api-app",level:3},{value:"Database",id:"database",level:3},{value:"Deployment",id:"deployment",level:2}];function h(e){const t={a:"a",h1:"h1",h2:"h2",h3:"h3",header:"header",img:"img",li:"li",p:"p",strong:"strong",ul:"ul",...(0,i.R)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsx)(t.header,{children:(0,n.jsx)(t.h1,{id:"architecture-overview",children:"Architecture Overview"})}),"\n",(0,n.jsx)(t.h2,{id:"context",children:"Context"}),"\n",(0,n.jsxs)(t.p,{children:["This solution is an implementation of the ",(0,n.jsx)(t.a,{href:"https://docs.cloud.coinbase.com/rosetta/docs/welcome",children:"Rosetta API"})," specification for Cardano Blockchain."]}),"\n",(0,n.jsxs)(t.p,{children:["Here and below we use ",(0,n.jsx)(t.a,{href:"https://en.wikipedia.org/wiki/C4_model",children:"C4"})," notation to describe the solution architecture."]}),"\n",(0,n.jsx)(t.p,{children:(0,n.jsx)(t.strong,{children:"Figure 1. Context Diagram"})}),"\n",(0,n.jsx)(t.p,{children:(0,n.jsx)(t.img,{alt:"Context Diagram",src:a(3030).A+""})}),"\n",(0,n.jsxs)(t.p,{children:["The specific changes in this implementation can be found in ",(0,n.jsx)(t.a,{href:"https://github.com/cardano-foundation/cardano-rosetta-java/blob/main/docs/cardano-specific-api-additions.md",children:"cardano-specific-api-additions"})]}),"\n",(0,n.jsx)(t.p,{children:"To use this Rosetta API for Cardano you can build the project from source or use the pre-built docker image."}),"\n",(0,n.jsx)(t.p,{children:(0,n.jsx)(t.a,{href:"https://hub.docker.com/orgs/cardanofoundation/repositories?search=rosetta-java",children:"Docker Images"})}),"\n",(0,n.jsx)(t.p,{children:"The solution provides Construction API (mutation of data) and Data API (read data) according to the Rosetta spec accessible via an REST API that allows you to interact with the Cardano blockchain."}),"\n",(0,n.jsx)(t.h2,{id:"implementation",children:"Implementation"}),"\n",(0,n.jsx)(t.p,{children:"The solution consists of the following four components:"}),"\n",(0,n.jsxs)(t.ul,{children:["\n",(0,n.jsx)(t.li,{children:"Cardano Node"}),"\n",(0,n.jsxs)(t.li,{children:["Yaci Indexer App (extended from ",(0,n.jsx)(t.a,{href:"https://github.com/bloxbean/yaci-store",children:"yaci-store"}),")"]}),"\n",(0,n.jsx)(t.li,{children:"Rosetta API App"}),"\n",(0,n.jsx)(t.li,{children:"Database"}),"\n"]}),"\n",(0,n.jsx)(t.p,{children:"This solution relies on the Cardano Node to provide the blockchain data. The Cardano Node is a full node that stores the entire history of the Cardano blockchain. The Cardano Node is used to query the blockchain data and to submit transactions to the blockchain."}),"\n",(0,n.jsx)(t.p,{children:"Yaci Indexer App retrieves data on per block basis from the Cardano Node and stores it in a database. The data stored in efficient way that is only required by the Rosetta API."}),"\n",(0,n.jsxs)(t.p,{children:["Rosetta API App in case of ",(0,n.jsx)(t.a,{href:"https://docs.cloud.coinbase.com/rosetta/docs/data-api-overview",children:"Data API"})," read data from the database and in case of ",(0,n.jsx)(t.a,{href:"https://docs.cloud.coinbase.com/rosetta/docs/construction-api-overview",children:"Construction API"})," it uses Cardano Node to submit transactions to the blockchain."]}),"\n",(0,n.jsx)(t.p,{children:(0,n.jsx)(t.strong,{children:"Figure 2. Component Diagram"})}),"\n",(0,n.jsx)(t.p,{children:(0,n.jsx)(t.img,{alt:"Component Diagram",src:a(8514).A+""})}),"\n",(0,n.jsx)(t.h3,{id:"cardano-node",children:"Cardano Node"}),"\n",(0,n.jsxs)(t.p,{children:["The ",(0,n.jsx)(t.a,{href:"https://github.com/IntersectMBO/cardano-node#overview-of-the-cardano-node-repository",children:"Cardano node"})," is the top-level component within the network. Network nodes connect to each other within the networking layer, which is the driving force for delivering information exchange requirements. This includes new block diffusion and transaction information for establishing a better data flow. Cardano nodes maintain connections with peers that have been chosen via a custom peer-selection process.\n",(0,n.jsx)(t.a,{href:"https://docs.cardano.org/learn/cardano-node",children:"https://docs.cardano.org/learn/cardano-node"})]}),"\n",(0,n.jsx)(t.h3,{id:"yaci-indexer-app",children:"Yaci Indexer App"}),"\n",(0,n.jsxs)(t.p,{children:["For indexing data from Cardano Blockchain we are using ",(0,n.jsx)(t.a,{href:"https://github.com/bloxbean/yaci-store",children:"yaci-store"})," project. This project provides a set of Spring Boot starters with customization possibilities."]}),"\n",(0,n.jsx)(t.p,{children:"To limit data footprint we use a set of mappers to map Cardano Blockchain block data to the data only required by the Rosetta API."}),"\n",(0,n.jsx)(t.h3,{id:"rosetta-api-app",children:"Rosetta API App"}),"\n",(0,n.jsx)(t.p,{children:"The Rosetta API App is a Spring Boot application that provides a REST API for interacting with the Cardano blockchain."}),"\n",(0,n.jsxs)(t.p,{children:["For ",(0,n.jsx)(t.a,{href:"https://docs.cloud.coinbase.com/rosetta/docs/data-api-overview",children:"Data API"})," it reads aggregated data from the database."]}),"\n",(0,n.jsxs)(t.p,{children:["For ",(0,n.jsx)(t.a,{href:"https://docs.cloud.coinbase.com/rosetta/docs/construction-api-overview",children:"Construction API"})," it sends transactions into Cardano Blockchain using ",(0,n.jsx)(t.a,{href:"https://github.com/bloxbean/cardano-client-lib",children:"cardano-client-lib"})]}),"\n",(0,n.jsx)(t.h3,{id:"database",children:"Database"}),"\n",(0,n.jsx)(t.p,{children:"You can use any relational database, such as MySql, Postgres SQL or H2 database.\nThe scheme is created automatically by the application\n(JPA)."}),"\n",(0,n.jsx)(t.h2,{id:"deployment",children:"Deployment"}),"\n",(0,n.jsx)(t.p,{children:"We provide two modes of deployment:"}),"\n",(0,n.jsxs)(t.ul,{children:["\n",(0,n.jsxs)(t.li,{children:["All-in-one container mode (packing everything into one container) ",(0,n.jsx)(t.a,{href:"./4.-Getting-Started-with-Docker",children:"More details"})]}),"\n",(0,n.jsxs)(t.li,{children:["Docker compose with multiple containers ",(0,n.jsx)(t.a,{href:"https://github.com/cardano-foundation/cardano-rosetta-java/wiki/4.-Getting-Started-with-Docker#how-to-run-with-docker-compose",children:"More details"})]}),"\n"]}),"\n",(0,n.jsxs)(t.p,{children:[(0,n.jsx)(t.strong,{children:"Figure 3. Container Diagram"}),"\n",(0,n.jsx)(t.img,{src:"https://github.com/cardano-foundation/cardano-rosetta-java/assets/15213725/9b77e714-01cc-4401-96ea-4f4bbc3a4b1d",alt:"containerDiagram"})]})]})}function l(e={}){const{wrapper:t}={...(0,i.R)(),...e.components};return t?(0,n.jsx)(t,{...e,children:(0,n.jsx)(h,{...e})}):h(e)}}}]);