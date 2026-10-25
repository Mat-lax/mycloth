
import React from "react";
import type { ReactComponentProps } from "../react-component/component-registry";

export default function HelloReact({
  title,
  api,
}: ReactComponentProps) {
  return (
    <section className="hello-react">
      <h2>{title || "Hello from MyCloth!"}</h2>
      <p>This component was loaded lazily.</p>

    </section>
  );
}
