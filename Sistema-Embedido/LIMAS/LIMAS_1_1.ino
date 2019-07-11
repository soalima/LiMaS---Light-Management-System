#include<stdlib.h>
const int sampleWindow = 50;
unsigned int signalMin;
const int BUZZERPin = 44;
int LEDPin1 = 12;
int LEDPin2 = 13;
int i;
int MICPin = A8;
byte PIRPin = 34;
int pir_value;
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
unsigned long millisflag;
unsigned long inicio1;
unsigned long fin1;
unsigned long Tiempo_Parcial1;
unsigned long Tiempo_Total1 = 0;
unsigned long inicio2;
unsigned long fin2;
unsigned long Tiempo_Parcial2;
unsigned long Tiempo_Total2 = 0;


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
#define RESET1 '8'
#define RESET2 '9'
#define SIGNAL_MIN 1024
#define MAX_AMB 960
#define MAX_LED 160
#define MIN_LED 0
#define PERIODO_ALARMA 250
#define PERIODO_LED 150



void setup() {
  pinMode(BUZZERPin, OUTPUT);
  noTone(BUZZERPin);
  pinMode(LEDPin1, OUTPUT);
  pinMode(PIRPin, INPUT); 
  pinMode(MICPin, INPUT); 
  Serial.begin(9600);
  Serial3.begin(19200);
}


void loop() 
{  
   char aux;
   unsigned int sample;   
   unsigned int umbral=mediaSonido()*8+signalMin;
   unsigned long stopMillis= millis()+1000;   
   int lectura_Infrarrojo;       
   int Tiempo_Alarna = 20000;
   

   while (millis() < stopMillis)
   {                 
    
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
    if( Verificar_Sensor_Sonido(umbral,encenderLed1) == 1 )
    {
    
      encenderLed1 = TRUE;
      flagMic = 1;
    }
    else
    {
      
      encenderLed1 = FALSE;
      flagMic=0;
    }
    if( Verificar_Bluetooth()==1)
    {

      flagMic = 1;
    
    }
    else
    {

      flagMic = FALSE;
            
    }
  }
}

int Verificar_Sensor_Infrarrojo(int ultimo_estado)
{

  if( digitalRead(PIRPin) == TRUE && flagMic==FALSE)
  {                   
      Encender_Led1();                     
      encenderLed1 = TRUE;
      return TRUE;     
  }
  else if( flagMic==0) 
  {      
     Apagar_Led1();         
     encenderLed1 = FALSE;
     return FALSE;    
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
        tone(BUZZERPin, TONO);    
        analogWrite(LEDPin1,HIGH);    
        analogWrite(LEDPin2,LOW);                           
        }
        millisAlarma = millis()+PERIODO_ALARMA;
        while(millis()< millisAlarma){
        noTone(BUZZERPin); 
        analogWrite(LEDPin2,HIGH);
        analogWrite(LEDPin1,LOW);   
        }
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
      Serial.println("apague 2!");   
       Apagar_Led2(); 
       encenderLed2 = FALSE;
       return FALSE;             
    }
    if( c == SHAKE2)
    {
      Serial.println(contador);
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
      }
    }
    
    if( c == RESET1)
    {
      analogWrite(LEDPin1,LOW);  
      encenderLed1 = FALSE;
      Tiempo_Total1 = 0;
      return FALSE;
    }
    
      if( c == '9')
    {
      analogWrite(LEDPin2,LOW);  
      encenderLed2 = FALSE;
      Tiempo_Total2 = 0;
      return FALSE;
    }
    
        
  }
  return encenderLed1;  
}
 

int Verificar_Sensor_Sonido( int media , int ultimo_estado)
{
  int sample;
  sample = analogRead(MICPin);  
  if(sample > media)
  {
    if(ultimo_estado==FALSE)
    {
      millisPrender = millis()+PERIODO_LED;
      while(millis()< millisPrender)
          Encender_Led1();
      return TRUE;
      
    }
    else
    {
      millisApagar = millis()+PERIODO_LED;
      while(millis()< millisApagar)
          Apagar_Led1();   
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
  unsigned long altomillis = millis()+5;
  while(millis()< altomillis){
  enviartiempo (Tiempo_Total1, "L1:");  
  }  
  Serial.println("apague 1!"); 
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
  encenderLed2=FALSE;
  enviartiempo (Tiempo_Total2, "L2:");
}

int mediaSonido(){
  unsigned long startMillis= millis();                                                                                                       
 
   unsigned int signalMax = FALSE;
   signalMin = SIGNAL_MIN;
   unsigned int sample;
   while (millis() - startMillis < sampleWindow)
   {
      sample = analogRead(MICPin);
      if (sample < SIGNAL_MIN)
      {
         if (sample > signalMax)
         {
            signalMax = sample;
         }
         else if (sample < signalMin)
         {
            signalMin = sample;
         }
      }
   }

   return (signalMax - signalMin)/2;
   
}

int regularIntensidadLuminica(int lectura_FotoResistencia){
    float deltaLuzAmbiente = MAX_AMB - MAX_LED;
    int minimoLed = MIN_LED;
    int maximoLed = MAX_LED;
    float deltaPotenciaLed = maximoLed - minimoLed;
    float porcentajeLuzAmbiente = (lectura_FotoResistencia - deltaPotenciaLed)/deltaLuzAmbiente;
    float porcentajeIluminacionLED = 1-porcentajeLuzAmbiente;
    int valorLed = minimoLed + (porcentajeIluminacionLED * deltaPotenciaLed);
    return valorLed;
}

void enviartiempo(unsigned long tiempo, String led){
    Serial.print("tiempo encendido \t");
    Serial.print(led);
    Serial.print("\t");
    Serial.println(tiempo);
    String regex = ":";
    String resultado = led + tiempo + regex;
    Serial.print(resultado);
    Serial3.print(resultado);
}
