package com.monterosasdk

import android.content.Context
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup

open class WrappedViewGroup<T: View>(context: Context) : ViewGroup(context) {

  private var wrappedView: T? = null

  // Method to replace the wrapped view with a new instance
  fun replaceWrappedView(newView: T) {
    // Remove the old wrapped view
    wrappedView?.let {
      removeView(it)
      didRemoveView(it)
    }

    // Add the new wrapped view
    wrappedView = newView
    newView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    addView(newView)
    didAddView(newView)

    forceLayoutInReactNative(newView)
  }

  /*
  ReactNative seems to handle layouts on a weird way which resulted in views failing to position on
  screen.

  There's a long running issue here where a solution similar to this was proposed in 2018, and still
  seems to do the trick: https://github.com/facebook/react-native/issues/17968
   */
  private fun forceLayoutInReactNative(newView: T) {
    Choreographer.getInstance().postFrameCallback {
      newView.measure(
        MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
      )
      newView.layout(0, 0, newView.measuredWidth, newView.measuredHeight)
      viewTreeObserver.dispatchOnGlobalLayout()
      }
  }

  open fun didAddView(experience: T) {
    // nop
  }

  open fun didRemoveView(experience: T) {
    // nop
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    // Layout the wrapped view to occupy the entire space
    wrappedView?.layout(0, 0, right - left, bottom - top)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    // Measure the wrapped view to occupy the entire space
    wrappedView?.measure(widthMeasureSpec, heightMeasureSpec)
    setMeasuredDimension(
      MeasureSpec.getSize(widthMeasureSpec),
      MeasureSpec.getSize(heightMeasureSpec)
    )
  }
}
