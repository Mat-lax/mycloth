
import { lazy, type ComponentType, type LazyExoticComponent } from "react";

export interface ReactComponentProps {
  title?: string;
  api?: string;
}

type LazyComponent = LazyExoticComponent<
  ComponentType<ReactComponentProps>
>;

export const COMPONENTS: Record<string, LazyComponent> = {
  "hello-react": lazy(
    () => import("../hello-react/HelloReact")
  ),
};
