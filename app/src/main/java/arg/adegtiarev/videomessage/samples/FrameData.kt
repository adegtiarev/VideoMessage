package arg.adegtiarev.liveletters.data

data class FrameData(
    val selectionStart: Int,
    val selectionEnd: Int,
    val scrollX: Int,
    val scrollY: Int,
    val viewCompoundPaddingLeft: Int,
    val viewWidth: Int,
    val text: String
)