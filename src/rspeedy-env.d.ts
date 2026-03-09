/// <reference types="@lynx-js/rspeedy/client" />

declare namespace JSX {
  interface IntrinsicElements {
    input: React.DetailedHTMLProps<
      React.InputHTMLAttributes<HTMLInputElement> & {
        bindinput?: (e: { value?: string; detail?: { value?: string } }) => void
      },
      HTMLInputElement
    >
    'explorer-input': {
      className?: string
      value?: string
      placeholder?: string
      bindinput?: (e: { value?: string; detail?: { value?: string } }) => void
    }
  }
}
