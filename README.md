# Compass

This project is under development for learning purpose

***W/ MAIN SCREEN***

[SCOPE]
La aplicación consta de botón FLOAT y una COMPASS VIEW.
Pulsando el botón Float la brújula se hace flotante y mediante una pulsación prolongada se puede
posicionar en otro lugar de la pantalla, además con un click sencillo se vuleve a la pantalla principal.

[EXPLICACION]
Al abrir la app se ejecuta el onCreate #onCreate MA
- Se carga el layout activity_main (y brújula)
    - Botón FLOAT
    - Brújula: View customizada, diseñada en un kotlin file llamado CompassView sobrescribiendo la
    función onDraw y definiendo un canvas sobre el que se usan los métodos drawCicle, drawText,
    drawLine... #onDraw
- Se define un sensor manager para posteriormente ser usado por la brújula
- Se para el servicio de brujula flotante en caso de que esté funcionando (por ejemplo la brujula
está flotante y se vuelve  abrir la app). Si no hay servicio continua sin devolver error. #stopService
- Se activa un onClicklistener para el botón FLOAT.

Al pulsar el botón FLOAT por primera vez, la aplicación necesita permisos para mostrar la brújula
que se solapará y mantendran sobre tood lo demás. Para pedir estos permisos es necesario añadir una
linea #SYSTEM_ALERT_WINDOW al manifest. Así avisamos al SO Android que nuestra app necesita permisos
para mostrar su contenido por encima de cualquier cosa. Después ejecuta la función
#checkOverlayPermission que no verifica más que si tiene ya dichos permisos concedidos en versiónes
android M o superiores. En caso de no tener permisos llama a la función #requestFloatingWindowPermission
que crea un alertDialogBuilder donde se especificará el título, mensaje y botones (máximo 3).
Un diálogo es una pequeña ventana que salta para que el usuario tome una decisión o introduzca
información adicional. Aquí se informará al usuario que la app requiere overlay permissions. En el
positiveButton a parte de especificar el mensaje también enviamos un intent para abrir las settings
en el apartado overlay y pasar el nombre de la app. Finalmente se crea el alertDialog (a traves del
alertDialogBuilder) y se muestra en pantalla.

Para leer los datos de los sensores solo se puede hacer implementando el interface #SensorEventListener,
este a su vez necesita que se sobrescriba onSensorChanged and onAccuracyChanged. La recolección de
datos ocurrirá hasta que la app vaya a onPause. Es necesario también crear una serie de arrays de 
variable flotante donde almacenar los datos del sensor.
Cuando el sistema detecta en los sensores un cambio en la lectura y el evento coincide con 
#TYPE_ACCELEROMETER o #TYPE_MAGNETIC_FIELD entonces modifica las variables asignados a estos.
Despues el sensorManager calcula la #rotationMatrix a través de accelerometerReading y 
magnetometerReading. Luego se calcula los #orientationAngles a través de getOrientation.
En este array tendremos en la posición 0 la orientacioón del dispositivo, en la 1 el pitch
y en la 2 el roll. Por último pasamos el dato de la posición 0 a grados y lo metemos en la variable 
angle, después creamos un puntero de la compassView y aplicamos la rotación de la imagen descrita en
angle. En el estado onResume (cuando la aplicación se visualiza) hay que registrar un listener para
#registerListenerAccelerometer y para #registerListenerMagnetic. Y en el estado #onPause
(cuando la app no se visualiza) se desregistran los listeners.

[FLOATING BUTTONS]
Al añadir los botones F y X a la brújula flotante, en el layout parece que estan enrasados con el 
top y bottom de la compass view pero esto no es cierto cuando se carga la aplicación.
Los botones estan por encima. Al parecer cuando Android ejecuta las lineas del canvas lo hace de un 
tamaño determinado (comprobado con Log.v) y no hay forma de saber que tamaño tiene at runtime.
Hay gente que dice que algo tan facil como saber son las dimensiones del view es muy complicado de 
hacer en Android. Los botones tienen un style que les hace abultar mucho para un caracter (X o F).
Para eso se crea un archivo style.xml


        //CODE FOR FLOATING BUTTONS en FloatingWindow.kt despues de compassViewFloat.setOnClickListener
        floatView.findViewById<Button>(R.id.x_button_view).setOnClickListener {
            stopSelf()
            windowManager.removeView(floatView)
            
        }
        floatView.findViewById<Button>(R.id.f_button_view).setOnClickListener {
            stopSelf()
            windowManager.removeView(floatView)
            val back = Intent(this, MainActivity::class.java)
            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(back)
        }
        
      //style.xml
      <resources>
         <style
             name="edge_button" parent="TextAppearance.AppCompat.Button">
            <item name="android:minHeight">35dp</item>
            <item name="android:minWidth">35dp</item>
            <item name="android:padding">0dp</item>
         </style>
      </resources>

[IMPROVEMENTS]
- Añadir nivel de burbuja
- Añadir opciones en ventana flotante con un tap
- Quitar el putextra en intent para que el floating window use el if(angle!=0)? (tardará más en cargar la floating window)
- Make compass rotation smoother
        
- Rotate first time from the shorter direction
- Add code to make the rotation velocity always the same
- add calibration warning (popup window when accuracy <2)
- add calibration message
- add bubble level

***WO/ MAIN SCREEN***

[SCOPE]
Al abrir la app se abre directamente la ventana flotante. No hay pantalla principal. Splash screen?
 - Opción 1: Un tap en la brujula visualizaría otra ventana que se posicionara en el lado libre de
 la pantalla pero siempre pegada
    - Opción 1.1: Con dos ventanas flotantes
    - Opción 1.2: Con una ventana flotante y 2 views
 - Opción 2: Un tap en la brújula cerrara la brujula y se abrirá una pantalla de opciones en el 
centro, estilo al floating lyrics del Musixmatch.

[OPCION 1.1]
Para hacer 2 ventanas flotantes: He conseguido duplicar las brújulas flotantes con un único layout,
2 kotlin class (FloatingWindow y FloatingWindow2), crear 2 intents en main activity, y OJO añadir
otro service al manifest!!!

[OPCION 1.2]
Creo que también puede hacerse con un view en el mismo layout que ocupe lo mismo que la brújula y se 
vaya posicionando según la localización de la brújula. El espacio que ocupa la view total es la 
misma que si se oculta.
