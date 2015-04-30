package rww.ui.foaf

import scalacss.Defaults._



/**
 * Created by hjs on 30/04/15.
 */
object FoafStyles extends StyleSheet.Inline {

  val darkGrey =  0x24221f
  val mediumGrey = 0x938b7f
  val lightGrey =  0xe2e2e2
  val lighterGrey =  0xededed
  val lightestGrey =  0xf8f8f8
  val lightYellow =  0xfffdcb
  val lightRed =  0xffe6e6
  val red =  0xb80b5b
  val blue =  0x3fabd3
  val green =  0x0bb988
  val orange =  0xff793a
  val brown =  0x938b7f
//  val white = white
//  val black = black

  import dsl._

  val picture = styleC {
    val top = styleS(
      width(300 px),
      height(300 px),
      borderRadius(50 %%),
      overflow.hidden,
      float.right
    )
    val img = styleS(
      width(100 %%),
      height.auto
    )
    top.named('outer) :*: img.named('image)
  }

  val picTop =  style(
    width(300 px),
    height(300 px),
    borderRadius(50 %%),
    overflow.hidden,
    float.right
  )

  val img = style(
    width(100 %%),
    height.auto
  )

  val name = style(
    height(50 px),
    lineHeight(50 px),
    fontSize(50 px),
    marginTop(-15 px),
    whiteSpace.nowrap,
    textOverflow := "ellipsis",
    overflow.hidden
  )

  val surname = style(
    height(45 px),
    lineHeight(45 px),
    fontSize(30 px),
    whiteSpace.nowrap,
    textOverflow := "ellipsis",
    overflow.hidden
  )

  val company = style(
    height(20 px),
    lineHeight(20 px),
    fontSize(16 px),
    color.rgb(0x93,0x8b,0x7f),
    whiteSpace.nowrap,
    textOverflow := "ellipsis",
    overflow.hidden
  )


}
