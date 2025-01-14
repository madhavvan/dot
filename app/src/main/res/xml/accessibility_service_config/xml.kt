<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
android:description="@string/accessibility_service_description"
android:packageNames="com.whatsapp"
android:accessibilityEventTypes="typeWindowContentChanged|typeViewClicked"
android:accessibilityFeedbackType="feedbackSpoken"
android:notificationTimeout="100"
android:canRetrieveWindowContent="true"
android:settingsActivity="com.example.Autply.MyAccessibilityService" />
