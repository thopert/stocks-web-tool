function hideDialogOnSuccess(args, dialogWidgetVar) {
    if (args && !args.validationFailed) {
        PF(dialogWidgetVar).hide();
    }
}