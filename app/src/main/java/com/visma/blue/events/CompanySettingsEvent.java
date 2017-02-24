package com.visma.blue.events;

public class CompanySettingsEvent {

    private boolean usesSupplierInvoiceApproval;

    public CompanySettingsEvent(boolean usesSupplierInvoiceApproval) {
        this.usesSupplierInvoiceApproval = usesSupplierInvoiceApproval;
    }

    public boolean getUsesSupplierInvoiceApproval() {
        return usesSupplierInvoiceApproval;
    }
}
