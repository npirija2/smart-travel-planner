import type { ReactNode } from "react";

type ModuleLoadingProps = {
  label?: string;
};

type ModuleErrorProps = {
  message: string;
};

type ModuleEmptyProps = {
  title: string;
  description: string;
  action?: ReactNode;
};

export function ModuleLoading({ label = "Loading module data..." }: ModuleLoadingProps) {
  return (
    <div className="bg-white border-2 border-gray-300 rounded-lg p-8 text-center text-gray-600">
      {label}
    </div>
  );
}

export function ModuleError({ message }: ModuleErrorProps) {
  return (
    <div className="bg-red-50 border-2 border-red-300 rounded-lg p-6">
      <h3 className="font-medium text-red-900 mb-1">Something went wrong</h3>
      <p className="text-sm text-red-700">{message}</p>
    </div>
  );
}

export function ModuleEmpty({ title, description, action }: ModuleEmptyProps) {
  return (
    <div className="bg-white border-2 border-gray-300 rounded-lg p-10 text-center">
      <h3 className="text-xl font-medium mb-2">{title}</h3>
      <p className="text-sm text-gray-600 max-w-xl mx-auto">{description}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}
