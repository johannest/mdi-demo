import { LitElement, html, css } from 'lit';
import { customElement } from 'lit/decorators.js';

@customElement('unload-confirmation')
export class UnloadConfirmation extends LitElement {
    static styles = css`
    :host {
      display: block;
    }
  `;

    connectedCallback() {
        super.connectedCallback();
        // Bind the event handler so we can remove it later.
        this._boundBeforeUnloadHandler = this._beforeUnloadHandler.bind(this);
        window.addEventListener('beforeunload', this._boundBeforeUnloadHandler);
    }

    disconnectedCallback() {
        window.removeEventListener('beforeunload', this._boundBeforeUnloadHandler);
        super.disconnectedCallback();
    }

    /**
     * The beforeunload event handler. When called, it signals the browser to display a
     * confirmation dialog. Note: The exact text of the dialog is controlled by the browser.
     */
    _beforeUnloadHandler(event) {
        // Cancel the event and trigger the confirmation dialog.
        event.preventDefault();
        // Chrome requires returnValue to be set.
        event.returnValue = '';
        // Some browsers may show a generic message.
        return '';
    }

    render() {
        return html`
      <!--
        Your component can render additional UI here.
        For example, you might include some explanatory text or allow the user to disable the confirmation.
      -->
      <slot></slot>
    `;
    }
}