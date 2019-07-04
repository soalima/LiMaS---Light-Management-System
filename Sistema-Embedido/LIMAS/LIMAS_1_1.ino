#include<stdlib.h>
const int sampleWindow = 50; // Ancho ventana en mS (50 mS = 20Hz)
unsigned int signalMin;
const int BUZZERPin = 44; //pin del buzzer
int LEDPin1 = 12; //pin del led
int LEDPin2 = 13; //pin del led
int i;//Variable auxiliar para condicionales
int MICPin = A8;
byte PIRPin = 34; // Input for HC-S501
int pir_value; // Place to store read PIR Value
const int LDRPin = A3;
int LDR_value;
int encenderLed1=0;
int encenderLed2=0;
int flagMic=0;
int flagbluetooth=0;
unsigned long ledMillis1;  
unsigned long ledMillis2;     
int intensidad_luz;
int contador = 0;
char c = ' ';
char read_string[10];
int regulando=0;
unsigned long millisAlarma;
unsigned long millisPrender;
unsigned long millisApagar;
unsigned long inicio1; //se prendio el led
unsigned long fin1;    //se apago el led
unsigned long Tiempo_Parcial1; //lo que duro prendido una vez
unsigned long Tiempo_Total1 = 0; //lo que lleva prendido desde que inicio1 arduino
unsigned long inicio2; //se prendio el led
unsigned long fin2;    //se apago el led
unsigned long Tiempo_Parcial2; //lo que duro prendido una vez
unsigned long Tiempo_Total2 = 0; //lo que lleva prendido desde que inicio1 arduino


#define TRUE  1
#define FALSE  0
#define TONO 500
#define SALIR '0'
#define ENCENDER1 '1'
#define APAGAR1 '2'
#define ALARMA '3'
#define SHAKE1 '4'
#define ENCENDER2 '5'
#define APAGAR2 '6'
#define SHAKE2 '7'
#define SIGNAL_MIN 1024
#define MAX_AMB 960
#define MAX_LED 160
#define MIN_LED 0
#define PERIODO_ALARMA 250
#define PERIODO_LED 150



void setup() {
  pinMode(BUZZERPin, OUTPUT);  //defin1ir pin como salida del buzzer (pwm)
  noTone(BUZZERPin);
  pinMode(LEDPin1, OUTPUT);//Se inicializa como OUTPUT el pin a usar. Debe ser PWM
  pinMode(PIRPin, INPUT); 
  pinMode(MICPin, INPUT); 
  Serial.begin(9600);
  Serial3.begin(19200);
}


void loop() 
{  
  //encendido por sonido
   char aux;
   unsigned int sample;   
   unsigned int umbral=mediaSonido()*8+signalMin;
   unsigned long stopMillis= millis()+1000;   
   int lectura_Infrarrojo;       
   int Tiempo_Alarna = 20000; //Si el celu no esta conectado la alarma durara este tiempo, luego puede configurarse por bt.
   

   while (millis() < stopMillis)
   {                 
    
    // Actualizar la intenzidad de la luz
    if(encenderLed1==TRUE)
    {
      regulando = TRUE;
      Encender_Led1();
      
      regulando = FALSE;         
    }
  if(encenderLed2==TRUE)
    {
      regulando = TRUE;
      Encender_Led2();
      regulando = FALSE;
    }
    // Verificamos el estado del sensor de sonido para encender/apagar led
    if( Verificar_Sensor_Sonido(umbral,encenderLed1) == 1 )
    {
    
      encenderLed1 = TRUE;
      flagMic = 1;
    }//El flag del mic es para evitar que el sensor de movimiento me apague el led si lo prendemos por aplauso
    else
    {
      
      encenderLed1 = FALSE;
      flagMic=0;
    }
    

    // Activacióm del la luz por sensor de Bluetooth
    if( Verificar_Bluetooth()==1)//Verificar_Bluetooth() == 1 ) //Verificar_Bluetooth() == 1 )Verificar_Serial() == 1)
    {
    
      encenderLed1 = TRUE;
      flagMic = 1; //El flag del mic es para evitar que el sensor de movimiento me apague el led si lo prendemos por bluetooth
    
    }
    else
    {
      
      encenderLed1 = FALSE;
      flagMic = FALSE;
            
    }    

    if( Verificar_Sensor_Infrarrojo(encenderLed1) == TRUE )
    {
      encenderLed1 = TRUE;
           
    }
    else
    {
      encenderLed1 = FALSE;   
    }    
  }
}

int Verificar_Sensor_Infrarrojo(int ultimo_estado)
{

  // Activación del la luz por sensor de Bluetooth 
  if( digitalRead(PIRPin) == TRUE && flagMic==FALSE)
  {                   
      Encender_Led1();                     
      encenderLed1 = TRUE;
      return TRUE;     
  }
  else if( flagMic==0) 
  {      
      //Serial.println(digitalRead(PIRPin)); 
     Apagar_Led1();         
     encenderLed1 = FALSE;
     return FALSE;    
  }
  return encenderLed1;
  
}

int Verificar_Serial()
{
  char aux;
  char c;
  
  if (Serial.available())
  {
     
    c = Serial.read();     
    if( c == ENCENDER1)
    {              
      Encender_Led1();
      encenderLed1 = TRUE;
      return TRUE;    
    }
    if( c == APAGAR1)
    {      
       Apagar_Led1(); 
       encenderLed1 = FALSE;
       return FALSE;             
    }
    if( c == ALARMA)
    {
      
      while (c == ALARMA)
      {
        millisAlarma = millis()+PERIODO_ALARMA;
        while(millis()< millisAlarma){
        tone(BUZZERPin, TONO); //activa un tono de frecuencia determinada en un pin dado        
        analogWrite(LEDPin1,HIGH);    
        analogWrite(LEDPin2,LOW);                           
        }
        //delay(250);        

        millisAlarma = millis()+PERIODO_ALARMA;
        while(millis()< millisAlarma){
        noTone(BUZZERPin); 
        analogWrite(LEDPin2,HIGH);
        analogWrite(LEDPin1,LOW);   
        }
        //delay(250); 
        if(Serial.available()){
          aux = Serial.read();             
          if( aux == ALARMA )
            c = SALIR;
        }             
      }
      analogWrite(LEDPin2,LOW); 
      encenderLed2 = FALSE;  
      encenderLed1 = FALSE;      
      Apagar_Led1();
      
    }
    if( c == SHAKE1)
    {
      //contador++;
      if( encenderLed1 == FALSE )
      {
        Encender_Led1(); 
        Serial.println("encender por agitacion");             
        encenderLed1 = TRUE;
      }
      else if ( encenderLed1 == TRUE )
      {
        Apagar_Led1();        
        Serial.println("apagar por agitacion");
        encenderLed1 = FALSE;
      }
    }
    if( c == ENCENDER2)
    {              
      Encender_Led2();
      encenderLed2 = TRUE;

      return 0;    
    }
    if( c == APAGAR2)
    {      
       Apagar_Led2(); 
       encenderLed2 = FALSE;
       return FALSE;             
    }
    if( c == SHAKE2)
    {
     // Serial.println("entre");
      Serial.println(contador);
      //contador++;
      if( encenderLed2 == FALSE )
      {
        Encender_Led2(); 
        Serial.println("encender por agitacion");             
        encenderLed2 = TRUE;
      }
      else if ( encenderLed2 == TRUE )
      {
        Apagar_Led2();      
        Serial.println("apagar por agitacion");
        //encenderLed2 = 0;
      }
    }
    /*
    if( c == '8')
    {
      
        Serial.println(Tiempo_Total1);
    }
      if( c == '9')
    {
      Serial.println(Tiempo_Total2);
    }
    */
        
  }
  return encenderLed1;  
}

int Verificar_Bluetooth()
{
  char c;
  char aux;
  if (Serial3.available())
  {     
    c = Serial3.read();        
    Serial.println(c);
    if( c == ENCENDER1)
    { 
             
      Encender_Led1();
      encenderLed1 = TRUE;
      return TRUE;
    
    }
    if( c == APAGAR1)
    {
      
       Apagar_Led1();       
       encenderLed1 = FALSE;
       return FALSE;             
    }
    if( c == ALARMA)
    {
      
      while (c == ALARMA)
      {
        millisAlarma = millis()+PERIODO_ALARMA;
        while(millis()< millisAlarma){
        tone(BUZZERPin, TONO); //activa un tono de frecuencia determinada en un pin dado        
        analogWrite(LEDPin1,HIGH);    
        analogWrite(LEDPin2,LOW);                           
        }//delay(250);        


        millisAlarma = millis()+PERIODO_ALARMA;
        while(millis()< millisAlarma){
        noTone(BUZZERPin); 
        analogWrite(LEDPin2,HIGH);
        analogWrite(LEDPin1,LOW);   
        }//delay(250); 
        if(Serial3.available()){
          aux = Serial3.read();             
          if( aux == ALARMA )
            c = SALIR;
        }             
      }
      analogWrite(LEDPin2,LOW); 
      encenderLed2 = FALSE;  
      encenderLed1 = FALSE;      
      Apagar_Led1();
      
    }
    if( c == SHAKE1)
    {
      //contador++;
      if( encenderLed1 == FALSE )
      {
        Encender_Led1(); 
        Serial.println("encender por agitacion");             
        encenderLed1 = TRUE;
      }
      else if ( encenderLed1 == TRUE )
      {
        Apagar_Led1();        
        Serial.println("apagar por agitacion");
        encenderLed1 = FALSE;
      }
    }
    if( c == ENCENDER2)
    {              
      Encender_Led2();
      encenderLed2 = TRUE;

      return FALSE;    
    }
    if( c == APAGAR2)
    {      
       Apagar_Led2(); 
       encenderLed2 = FALSE;
       return FALSE;             
    }
    if( c == SHAKE2)
    {
     // Serial.println("entre");
      Serial.println(contador);
      //contador++;
      if( encenderLed2 == FALSE )
      {
        Encender_Led2(); 
        Serial.println("encender por agitacion");             
        encenderLed2 = TRUE;
      }
      else if ( encenderLed2 == TRUE )
      {
        Apagar_Led2();      
        Serial.println("apagar por agitacion");
        //encenderLed2 = FALSE;
      }
    }
    /*
    if( c == '8')
    {
      
       Serial.print("tiempo encendido led1 \t");
       Serial.println(Tiempo_Total1);
       String resultado = "L1: ";
       resultado = resultado + Tiempo_Total1;
       Serial3.println(resultado);
    }
      if( c == '9')
    {
      Serial.print("tiempo encendido led2 \t");
      Serial.println(Tiempo_Total2);
      String resultado = "L2: ";
       resultado = resultado + Tiempo_Total2;
      Serial3.println(resultado);
    }
    */
        
  }
  return encenderLed1;  
}
 

int Verificar_Sensor_Sonido( int media , int ultimo_estado)
{
  int sample;
  
  // Activacion de la luz por sensor de sonido
  sample = analogRead(MICPin);  
  if(sample > media)
  {
    if(ultimo_estado==FALSE)
    {
      millisPrender = millis()+PERIODO_LED;
      while(millis()< millisPrender)
          Encender_Led1();
     // delay(150);
      return TRUE;
      
    }
    else
    {
      millisApagar = millis()+PERIODO_LED;
      while(millis()< millisApagar)
          Apagar_Led1();     
      //delay(150);
      return FALSE;      

    }      
  }

  return encenderLed1;
    
}

int Encender_Led1()
{
  analogWrite(LEDPin1,regularIntensidadLuminica(analogRead(LDRPin)));
  if(regulando == FALSE)
  {
      inicio1 = millis();         
  }
}

int Apagar_Led1()
{
  if(encenderLed1==FALSE)
    return;
  analogWrite(LEDPin1,LOW);
  fin1 = millis();
  Tiempo_Parcial1 = (fin1 - inicio1);
  Tiempo_Total1 += Tiempo_Parcial1;
  encenderLed1=FALSE ; 
  enviartiempo (Tiempo_Total1, "L1:");
}

int Encender_Led2()
{
  analogWrite(LEDPin2,regularIntensidadLuminica(analogRead(LDRPin)));
  if(regulando == FALSE)
  {
       inicio2 = millis();         
  }
         
}

void Apagar_Led2()
{
  if(encenderLed2==FALSE)
    return;
  analogWrite(LEDPin2,LOW);
  fin2 = millis();
  Tiempo_Parcial2 = (fin2 - inicio2);
  Tiempo_Total2 += Tiempo_Parcial2;  
  encenderLed2=FALSE ; 
  enviartiempo (Tiempo_Total2, "L2:");
}

int mediaSonido(){
  unsigned long startMillis= millis();                                                                                                       
 
   unsigned int signalMax = FALSE;
   signalMin = SIGNAL_MIN;
 
   // Recopilar durante la ventana
   unsigned int sample;
   while (millis() - startMillis < sampleWindow)
   {
      sample = analogRead(MICPin);
      if (sample < SIGNAL_MIN)
      {
         if (sample > signalMax)
         {
            signalMax = sample;  // Actualizar máximo
         }
         else if (sample < signalMin)
         {
            signalMin = sample;  // Actualizar mínimo
         }
      }
   }

   return (signalMax - signalMin)/2;  // Amplitud del sonido
   
}

int regularIntensidadLuminica(int lectura_FotoResistencia){
    float deltaLuzAmbiente = MAX_AMB - MAX_LED; //luz maxima menos luz minima ambiente
    int minimoLed = MIN_LED;
    int maximoLed = MAX_LED;
    float deltaPotenciaLed = maximoLed - minimoLed; // delta valores del led
    float porcentajeLuzAmbiente = (lectura_FotoResistencia - deltaPotenciaLed)/deltaLuzAmbiente;
    float porcentajeIluminacionLED = 1-porcentajeLuzAmbiente;
    int valorLed = minimoLed + (porcentajeIluminacionLED * deltaPotenciaLed);
    return valorLed;
}

void enviartiempo(unsigned long tiempo, String led){
    Serial.print("tiempo encendido led1 \t");
    Serial.println(tiempo);
    String resultado = led + tiempo;
    Serial3.println(resultado);
}
