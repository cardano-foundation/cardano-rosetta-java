"use strict";(self.webpackChunkrosetta_docs=self.webpackChunkrosetta_docs||[]).push([[981],{5363:(e,t,n)=>{n.r(t),n.d(t,{default:()=>c});n(6540);var r=n(9136),o=n(4848);function c(e){let{children:t,fallback:n}=e;return(0,r.A)()?(0,o.jsx)(o.Fragment,{children:t?.()}):n??null}},8203:function(e,t,n){var r=this&&this.__createBinding||(Object.create?function(e,t,n,r){void 0===r&&(r=n);var o=Object.getOwnPropertyDescriptor(t,n);o&&!("get"in o?!t.__esModule:o.writable||o.configurable)||(o={enumerable:!0,get:function(){return t[n]}}),Object.defineProperty(e,r,o)}:function(e,t,n,r){void 0===r&&(r=n),e[r]=t[n]}),o=this&&this.__setModuleDefault||(Object.create?function(e,t){Object.defineProperty(e,"default",{enumerable:!0,value:t})}:function(e,t){e.default=t}),c=this&&this.__importStar||function(e){if(e&&e.__esModule)return e;var t={};if(null!=e)for(var n in e)"default"!==n&&Object.prototype.hasOwnProperty.call(e,n)&&r(t,e,n);return o(t,e),t},i=this&&this.__importDefault||function(e){return e&&e.__esModule?e:{default:e}};Object.defineProperty(t,"__esModule",{value:!0});const a=i(n(5363)),s=i(n(1410)),l=c(n(6540));n(9379);class d extends l.Component{observer=null;constructor(e){super(e),this.mutationCallback=this.mutationCallback.bind(this),"undefined"!=typeof window&&(this.observer=new MutationObserver(this.mutationCallback))}componentWillUnmount(){document.dispatchEvent(new Event("scalar:destroy-references")),this.observer?.disconnect()}mutationCallback(e){e.forEach((e=>{if("childList"===e.type){const e=document.getElementById("api-reference-container");if(e&&this.props.route.configuration&&!document.getElementById("api-reference")){console.log("Loading Scalar script...");const t=JSON.parse(JSON.stringify(this.props.route.configuration)),n=document.createElement("script");n.id="api-reference",n.type="application/json",e.appendChild(n);if(document.body.getAttribute("data-scalar-loaded"))console.log("Scalar script already loaded, reloading app"),document.dispatchEvent(new Event("scalar:reload-references")),document.dispatchEvent(new CustomEvent("scalar:update-references-config",{detail:{configuration:t}}));else{"function"==typeof t?.content&&(t.content=t.content());const r=t?.content?"string"==typeof t?.content?t.content:JSON.stringify(t.content):"";t?.content&&delete t.content;const o=JSON.stringify(t??{}).split('"').join("&quot;");n.dataset.configuration=o,n.innerHTML=r;const c=document.createElement("script");c.src="https://cdn.jsdelivr.net/npm/@scalar/api-reference",c.async=!0,c.onload=()=>{console.log("Scalar script loaded successfully"),document.body.setAttribute("data-scalar-loaded","true")},c.onerror=e=>{console.error("Error loading Scalar script:",e)},e.appendChild(c)}this.observer?.disconnect()}}}))}setupAPIReference=()=>{"undefined"!=typeof window&&(this.observer=new MutationObserver(this.mutationCallback),this.observer.observe(document.body,{childList:!0,subtree:!0}))};render(){return l.default.createElement(s.default,null,l.default.createElement(a.default,null,(()=>("undefined"!=typeof window&&this.setupAPIReference(),l.default.createElement("div",{id:"api-reference-container"})))))}}t.default=d},9379:(e,t,n)=>{n.r(t)}}]);