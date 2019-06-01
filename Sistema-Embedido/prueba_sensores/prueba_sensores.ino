/**PRUEBA DE BUZZER**/

const int BUZZERPin = 1; //pin del buzzer
int LEDPin = 13; //pin del led
int i;//Variable auxiliar para condicionales
int MICPin = A4;
byte PIRPin = 34; // Input for HC-S501
int pir_value; // Place to store read PIR Value
const int LDRPin = A3;
int LDR_value;
int encenderLed=0;
int flagMic=0;

void setup() {
  pinMode(BUZZERPin, OUTPUT);  //definir pin como salida del buzzer (pwm)
  pinMode(LEDPin, OUTPUT);//Se inicializa como OUTPUT el pin a usar. Debe ser PWM
  pinMode(PIRPin, INPUT); 
    pinMode(MICPin, INPUT); 

  Serial.begin(9600);
}/******

void loop(){
   int microfono_value;
  String microfono_output = String("Microfono: ");
  String pir_output = String("PIR: ");
  String LDR_output = String("LDR: ");
  
  digitalWrite(BUZZERPin, HIGH);   // poner el Pin en HIGH
  delay(20);               // esperar 20 milisegundos
  digitalWrite(BUZZERPin, LOW);    // poner el Pin en LOW
  delay(500);               // esperar 500 milisegundos*/
  /*
  //Se recorren los valores desde el 0 al 255 para enviar una señal PWM con ciclo de trabajo de 0% a 100%
  //aumentando en 2.55% el ciclo de trabajo cada 10 mili segundos.  
  for(i=50; i<210; i++){ 
    analogWrite(LEDPin,i);
    delay(10);
  }
  
  //Se recorren los valores del 255 al 0 de mayor a menor igual que el for anterior.
  for(i=210; i>49; i--){
    analogWrite(LEDPin,i);
    delay(10);
  }
  
  digitalWrite(BUZZERPin, HIGH);  //buzzer
  
  //analogWrite(LEDPin, 50); //led
  
  microfono_value = analogRead(MICPin); //leo mic
  if(microfono_value > 100){
    microfono_output += microfono_value;
    Serial.println("Volumen alto");
    Serial.println(microfono_output);  //muestro valor mic
  }

  delay(1000); //tiempo minimo que tarda en leer el sensor de movimiento
  if(digitalRead(PIRPin) == HIGH){
    pir_output += pir_value; 
    Serial.println("Detectado movimiento");
    /*Serial.println(pir_output); //muestro valor pin
    analogWrite(LEDPin, 50); //led
  }else{
    Serial.println("Movimiento no detectado");
      analogWrite(LEDPin, 0); //led
    }

   LDR_value = analogRead(LDRPin);
   LDR_output += LDR_value;
   Serial.println(LDR_output);
   /*
    if(LDR_value > 900)
      analogWrite(LEDPin, 0); //led
    else if (LDR_value < 900 && LDR_value > 250)
      analogWrite(LEDPin, 50); //led
    else if (LDR_value < 260)
      analogWrite(LEDPin, 210); //led
     */  /*
     //analogWrite(LEDPin,970-LDR_value/5);

    float deltaLuzAmbiente = 960 - 160; //luz maxima menos luz minima ambiente
    int minimoLed = 0;
    int maximoLed = 250;
    float deltaPotenciaLed = maximoLed - minimoLed; // delta valores del led
    float porcentajeLuzAmbiente = (LDR_value - deltaPotenciaLed)/deltaLuzAmbiente;
    Serial.println(porcentajeLuzAmbiente);
    float porcentajeIluminacionLED = 1- porcentajeLuzAmbiente;
    Serial.println(porcentajeIluminacionLED);
    int valorLed = minimoLed + (porcentajeIluminacionLED * deltaPotenciaLed);
    Serial.println(valorLed);
    analogWrite(LEDPin, valorLed);
*/

    //esto se repite para cada nodo digamos
void loop(){
   //Apago o enciendo ante un pico de sonido
  int lectura_Microfono = analogRead(MICPin);
  if(lectura_Microfono > 90)
    {
      Serial.println("SONIDO FUERTE LCOO BAJEN EL VLOUEM");
      Serial.println(lectura_Microfono);  
    }
  if(lectura_Microfono > 100)
    {
      Serial.println("VOLUMEN ALTO");
      if(encenderLed==1){
          encenderLed=0;
          flagMic=0;
      }else if(encenderLed==0){
          encenderLed=1;

          flagMic=1; //hay que ignorar el tema del movimiento para este caso
      }
    }
  
    //No apago si no hay movimiento, en caso de que haya sido prendida por aplauso
    int lectura_Infrarrojo = digitalRead(PIRPin);
    if(lectura_Infrarrojo==1){
        
        encenderLed=1;
    }else if(flagMic==0){
      if(lectura_Infrarrojo==0){
        encenderLed=0;}
    }
    
    if(encenderLed==1){
        int lectura_FotoResistencia = analogRead(LDRPin);
        analogWrite(LEDPin,regularIntensidadLuminica(lectura_FotoResistencia));//regularIntensidadLuminica convierte la lectura de la resistencia a los valores del pwm del led
               delay(8500); 

       /* 
        if(corroborarSospechas(LEDPin)==TRUE){
            enviarNotificacion();
            digitalWrite(BUZZERPin, HIGH); //alarma
            esperaBloqueante(); //el sistema la queda acá esperando a que resetee el sistema con el celular
        }
        */
    }else{
      Serial.println("asdf");
      analogWrite(LEDPin,LOW);
    }
    
}

int regularIntensidadLuminica(int lectura_FotoResistencia){
    float deltaLuzAmbiente = 960 - 160; //luz maxima menos luz minima ambiente
    int minimoLed = 0;
    int maximoLed = 250;
    float deltaPotenciaLed = maximoLed - minimoLed; // delta valores del led
    float porcentajeLuzAmbiente = (lectura_FotoResistencia - deltaPotenciaLed)/deltaLuzAmbiente;
    float porcentajeIluminacionLED = 1- porcentajeLuzAmbiente;
    int valorLed = minimoLed + (porcentajeIluminacionLED * deltaPotenciaLed);
    //Serial.println(valorLed);
    return valorLed;
  }



/**PRUEBA DE LED**/

/*------Declaración de variables-----*/



/**PRUEBA DE MICROFONO**/
/*
const int pinMicrophone = A2;

/*void setup ()
{
  Serial.begin(9600);
}
*//*
void loop ()
{
  int valorSonido = analogRead(pinMicrophone);
  if(valorSonido > 90)
    {
      Serial.println("SONIDO FUERTE LCOO BAJEN EL VLOUEM");
      Serial.println(valorSonido);  
    }
}
*/
/**PRUEBA DE SENSOR DE LUZ**/
/*
const int LDRPin = A2;
 
void setup()
{
 Serial.begin(9600);
} 
 
void loop()
{
   int value = analogRead(LDRPin);
   
      Serial.println(value);  
      delay(500);
      
   
}
*/
/**PRUEBA DE SENSOR DE MOVIMIENTO**/
/*
// HC-SR501 Motion Detector
// Sample Sketch

int pirPin = 34; // Input for HC-S501

int pirValue; // Place to store read PIR Value


void setup() {
  
  pinMode(pirPin, INPUT); 
}

void loop() {
  pirValue = digitalRead(pirPin);
  Serial.println(pirValue);
}*/
