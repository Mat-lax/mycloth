
import React, { Suspense } from "react";
import { createRoot, type Root } from "react-dom/client";
import { COMPONENTS } from "./component-registry";

class ReactComponentElement extends HTMLElement {
  private root: Root | null = null;
  private loadVersion = 0;

  connectedCallback() {
    console.log("MYCLOTH: connectedCallback executed");
    if (this.root) return;

    const version = ++this.loadVersion;
    this.root = createRoot(this);

    const componentName = this.dataset.component;
    const Component = componentName
      ? COMPONENTS[componentName]
      : undefined;

    if (!Component) {
      this.root.render(
        <p role="alert">
          Unknown React component: {componentName || "(missing name)"}
        </p>
      );
      return;
    }

    this.root.render(
      <Suspense fallback={<p>Loading component...</p>}>
        <Component
          title={this.dataset.title}
          api={this.dataset.api}
        />
      </Suspense>
    );
    console.log("[React] Test render submitted");
  }

  disconnectedCallback() {
    this.loadVersion++;

    this.root?.unmount();
    this.root = null;
  }
}

if (!customElements.get("react-component")) {
  customElements.define(
    "react-component",
    ReactComponentElement
  );
}
