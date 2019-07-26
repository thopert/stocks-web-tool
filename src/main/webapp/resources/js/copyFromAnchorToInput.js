function copyFromAnchorToInput(anchorId, inputId) {
    var fromElement = document.getElementById(anchorId);

    var toElement = document.getElementById(inputId);

    toElement.value = fromElement.text;
}