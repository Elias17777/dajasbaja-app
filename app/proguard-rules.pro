# Reglas ProGuard para la app Dar de Baja
# Mantener clases de JavaMail para que no se eliminen en el build
-keep class javax.mail.** { *; }
-keep class javax.activation.** { *; }
-keep class com.sun.mail.** { *; }
-dontwarn javax.mail.**
-dontwarn javax.activation.**
-dontwarn com.sun.mail.**
