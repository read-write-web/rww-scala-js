package rww.ui.foaf

import scalacss.Defaults._


object FoafStyles extends StyleSheet.Inline {

  import dsl._

  //  val darkGrey =  0x24221f
  val mediumGrey = "#938b7f"
  //  val lightGrey =  0xe2e2e2
  val lighterGrey = "#ededed"
  val lightestGrey = "#f8f8f8"
  val myBlue = "#3fabd3"
  //  val lightYellow =  0xfffdcb
  //  val lightRed =  0xffe6e6
  //  val red =  0xb80b5b
  //  val blue =  0x3fabd3
  //  val green =  0x0bb988
  //  val orange =  0xff793a
  //  val brown =  0x938b7f
  //  val white = white
  //  val black = black

  val editProfile = style(
    width(40 px),
    height(40 px),
    backgroundColor.red,
    color.white,
    float.right,
    textTransform.uppercase,
    textAlign.center,
    lineHeight(40 px)
  )

  // note need to use import shapeless.singleton.syntax._ if using this
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

  //same as above but unsafe
  val pictureUnsafe = style(
    width(200 px),
    height(200 px),
    borderRadius(50 %%),
    overflow.hidden,
    position.absolute,
    right(35 px),
    unsafeChild("img")(
      width(110 %%),
      height.initial
    )
  )

  //small profile pix
  val pix = style(

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
    color(mediumGrey),
    whiteSpace.nowrap,
    textOverflow := "ellipsis",
    overflow.hidden
  )

  val basic = style(
    width(400 px)
  )


  val details = style(
    color(mediumGrey),
    unsafeChild(".title")(
      backgroundColor(myBlue),
      padding(7 px, 10 px),
      margin(30 px, 0 px),
      color.white
    ),
    unsafeChild(".title-case")(
      margin(0 px, 0 px, 5 px)
    ),
    unsafeChild(".content")(
      margin(0 px, 0 px, 20 px),
      whiteSpace.nowrap,
      textOverflow := "ellipsis",
      overflow.hidden
    ),
    unsafeChild("ul")(
      padding.`0`,
      display.flex,
//      height(200 px),
      //      maxWidth(100 %%),
      //      flexDirection.column,
      flexWrap.wrap,
      //      justifyContent.flexStart,
      //      alignItems.flexStart,
      //      alignContent.flexStart,
      unsafeChild("li")(
        width(300 px),
        margin(10 px),
        padding(10 px),
        alignSelf.auto,
        backgroundColor(lightestGrey),
        unsafeChild("input")(
          fontSize(16 px),
          lineHeight(30 px),
          height(20 px)
        )
      )
    )
  )


  // below taken from base.less
  val clearfix = style(
    &.before(display.table),
    &.after(clear.both)
    //    zoom := "0"
  )


  val center = style(margin(0 em, auto))

  val centerText = style(textAlign.center)

  val floatLeft = style(float.left)

  val titleCase = style("title-case")(
    fontWeight.bold,
    textTransform.uppercase,
    color("#000000")
  )

  val span3 = style(
    unsafeChild("li")(
      listStyleType := "none",
      width(32 %%),
      margin(0 px, 2 %%, 2 %%, 0 px),
      boxSizing.borderBox
    ),
    &.nthChild(2)(
      margin(0 px, 0 px, 2 %%, 0 px)
    )
  )

  val body = style(
    backgroundColor("#EDEDED"),
    minHeight(98 %%),
    width(100 %%),
    minWidth(1000 px),
    margin(0 px),
    marginTop(20 px),
    padding(0 px),
    height(100 %%),
    fontFamily := "'Noto Sans', sans-serif",
    fontSize(1 em),
    position.relative
  )

  val content = style(
    margin(0 px, 0 px, 20 px),
    whiteSpace.nowrap,
    textOverflow := "ellipsis",
    overflow.hidden
  )

  val contacts = style(
    display.flex,
    padding(0 px),
    //      height(200 px),
    //      maxWidth(100 %%),
    //      flexDirection.column,
    flexWrap.wrap
  )

  val contact = style(
    alignSelf.auto,
    height(150 px),
//    padding(0 px),
    backgroundColor(lightestGrey)
  )

  val contactName = style(
//    backgroundColor.blue,
    padding( 7 px, 10 px),
    margin(30 px, 0 px, 0 px, 0 px)
//    color.white
  )

  val contactPixOuterBox = style(
      position.relative,
      width(80 px),
      height(80 px),
      overflow.hidden,
      float.right)

  val contactPix = intStyle(0 to 1)(i=>
    styleS(
        border(1 px),
        position.absolute,
        zIndex(i),
        width(60 px),
        height(60 px),
        right(i*10 px),
        top(i*10 px),
        width(100 %%),
        height.auto
    )
  )

  val webIdBar = intStyle(0 to 100)(i =>
    styleS(
       backgroundColor(if (i<30) yellow else if (i<65) yellowgreen else green)
    )
  )


//  val submitButton = style (
//    hover,
//    boxShadow:="none",
//    outline.none,
//    background(green),
//    twist(-5deg, 1.1);
//    )
}
