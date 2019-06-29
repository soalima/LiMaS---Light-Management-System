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
unsigned long inicio1; //se prendio el led
unsigned long fin1;    //se apago el led
unsigned long Tiempo_Parcial1; //lo que duro prendido una vez
unsigned long Tiempo_Total1 = 0; //lo que lleva prendido desde que inicio1 arduino
unsigned long inicio2; //se prendio el led
unsigned long fin2;    //se apago el led
unsigned long Tiempo_Parcial2; //lo que duro prendido una vez
unsigned long Tiempo_Total2 = 0; //lo que lleva prendido desde que inicio1 arduino



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
    if(encenderLed1==1)
    {
      regulando = 1;
      Encender_Led1();
      
      regulando = 0;         
    }
  if(encenderLed2==1)
    Encender_Led2();
    // Verificamos el estado del sensor de sonido para encender/apagar led
    if( Verificar_Sensor_Sonido(umbral,encenderLed1) == 1 )
    {
    
      encenderLed1 = 1;
      flagMic = 1;
    }//El flag del mic es para evitar que el sensor de movimiento me apague el led si lo prendemos por aplauso
    else
    {
      
      encenderLed1 = 0;
      flagMic=0;
    }
    

    // Activacióm del la luz por sensor de Bluetooth
    if( Verificar_Bluetooth()==1)//Verificar_Bluetooth() == 1 ) //Verificar_Bluetooth() == 1 )Verificar_Serial() == 1)
    {
    
      encenderLed1 = 1;
      flagMic = 1; //El flag del mic es para evitar que el sensor de movimiento me apague el led si lo prendemos por bluetooth
    
    }
    else
    {
      
      encenderLed1 = 0;
      flagMic = 0;
            
    }    

    if( Verificar_Sensor_Infrarrojo(encenderLed1) == 1 )
    {
      encenderLed1 = 1;
           
    }
    else
    {
      encenderLed1 = 0;   
    }    
  }
}

int Verificar_Sensor_Infrarrojo(int ultimo_estado)
{

  // Activación del la luz por sensor de Bluetooth 
  if( digitalRead(PIRPin) == 1 && flagMic==0)
  {                   
      Encender_Led1();                     
      encenderLed1 = 1;
      return 1;     
  }
  else if( flagMic==0) 
  {      
      //Serial.println(digitalRead(PIRPin)); 
     Apagar_Led1();         
     encenderLed1 = 0;
     return 0;    
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
    if( c == '1')
    {              
      Encender_Led1();
      encenderLed1 = 1;
      return 1;    
    }
    if( c == '2')
    {      
       Apagar_Led1(); 
       encenderLed1 = 0;
       return 0;             
    }
    if( c == '3')
    {
      
      while (c == '3')
      {
        tone(BUZZERPin, 500); //activa un tono de frecuencia determinada en un pin dado        
        analogWrite(LEDPin1,250);    
        analogWrite(LEDPin2,LOW);                           
        delay(250);        

        noTone(BUZZERPin); 
        analogWrite(LEDPin2,250);
        analogWrite(LEDPin1,LOW);   
        delay(250); 
        if(Serial.available()){
          aux = Serial.read();             
          if( aux == '3' )
            c = '0';
        }             
      }
      analogWrite(LEDPin2,LOW); 
      encenderLed2 = 0;  
      encenderLed1 = 0;      
      Apagar_Led1();
      
    }
    if( c == '4')
    {
      //contador++;
      if( encenderLed1 == 0 )
      {
        Encender_Led1(); 
        Serial.println("encender por agitacion");             
        encenderLed1 = 1;
      }
      else if ( encenderLed1 == 1 )
      {
        Apagar_Led1();        
        Serial.println("apagar por agitacion");
        encenderLed1 = 0;
      }
    }
    if( c == '5')
    {              
      Encender_Led2();
      encenderLed2 = 1;

      return 0;    
    }
    if( c == '6')
    {      
       Apagar_Led2(); 
       encenderLed2 = 0;
       return 0;             
    }
    if( c == '7')
    {
     // Serial.println("entre");
      Serial.println(contador);
      //contador++;
      if( encenderLed2 == 0 )
      {
        Encender_Led2(); 
        Serial.println("encender por agitacion");             
        encenderLed2 = 1;
      }
      else if ( encenderLed2 == 1 )
      {
        Apagar_Led2();      
        Serial.println("apagar por agitacion");
        //encenderLed2 = 0;
      }
    }
    if( c == '8')
    {
      
        Serial.println(Tiempo_Total1);
    }
      if( c == '9')
    {
      Serial.println(Tiempo_Total2);
    }
        
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
    if( c == '1')
    { 
             
      Encender_Led1();
      encenderLed1 = 1;
      return 1;
    
    }
    if( c == '2')
    {
      
       Apagar_Led1();       
       encenderLed1 = 0;
       return 0;             
    }
    if( c == '3')
    {
      
      while (c == '3')
      {
        tone(BUZZERPin, 500); //activa un tono de frecuencia determinada en un pin dado        
        analogWrite(LEDPin1,250);    
        analogWrite(LEDPin2,LOW);                           
        delay(250);        

        noTone(BUZZERPin); 
        analogWrite(LEDPin2,250);
        analogWrite(LEDPin1,LOW);   
        delay(250); 
        if(Serial3.available()){
          aux = Serial3.read();             
          if( aux == '3' )
            c = '0';
        }             
      }
      analogWrite(LEDPin2,LOW); 
      encenderLed2 = 0;  
      encenderLed1 = 0;      
      Apagar_Led1();
      
    }
    if( c == '4')
    {
      //contador++;
      if( encenderLed1 == 0 )
      {
        Encender_Led1(); 
        Serial.println("encender por agitacion");             
        encenderLed1 = 1;
      }
      else if ( encenderLed1 == 1 )
      {
        Apagar_Led1();        
        Serial.println("apagar por agitacion");
        encenderLed1 = 0;
      }
    }
    if( c == '5')
    {              
      Encender_Led2();
      encenderLed2 = 1;

      return 0;    
    }
    if( c == '6')
    {      
       Apagar_Led2(); 
       encenderLed2 = 0;
       return 0;             
    }
    if( c == '7')
    {
     // Serial.println("entre");
      Serial.println(contador);
      //contador++;
      if( encenderLed2 == 0 )
      {
        Encender_Led2(); 
        Serial.println("encender por agitacion");             
        encenderLed2 = 1;
      }
      else if ( encenderLed2 == 1 )
      {
        Apagar_Led2();      
        Serial.println("apagar por agitacion");
        //encenderLed2 = 0;
      }
    }
    if( c == '8')
    {
      
       Serial.print("tiempo encendido led1 \t");
       Serial.println(Tiempo_Total1);
    }
      if( c == '9')
    {
      Serial.print("tiempo encendido led2 \t");
      Serial.println(Tiempo_Total2);
    }
        
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
    if(ultimo_estado==0)
    {
    
      Encender_Led1();
      delay(150);
      return 1;
      
    }
    else
    {
      
      Apagar_Led1();     
      delay(150);
      return 0;      

    }      
  }

  return encenderLed1;
    
}

int Encender_Led1()
{
  analogWrite(LEDPin1,regularIntensidadLuminica(analogRead(LDRPin)));
  if(regulando == 0)
  {
      inicio1 = millis();         
  }
}

int Apagar_Led1()
{
  if(encenderLed1==0)
    return;
  analogWrite(LEDPin1,LOW);
  fin1 = millis();
  Tiempo_Parcial1 = (fin1 - inicio1);
  Tiempo_Total1 += Tiempo_Parcial1;
  encenderLed1=0 ; 
}

int Encender_Led2()
{
  analogWrite(LEDPin2,regularIntensidadLuminica(analogRead(LDRPin)));

  inicio2 = millis();         
}

int Apagar_Led2()
{
  if(encenderLed2==0)
    return;
  analogWrite(LEDPin2,LOW);
  fin2 = millis();
  Tiempo_Parcial2 = (fin2 - inicio2);
  Tiempo_Total2 += Tiempo_Parcial2;  
  encenderLed2=0 ; 
}
int mediaSonido(){
  unsigned long startMillis= millis();                                                                                                       
 
   unsigned int signalMax = 0;
   signalMin = 1024;
 
   // Recopilar durante la ventana
   unsigned int sample;
   while (millis() - startMillis < sampleWindow)
   {
      sample = analogRead(MICPin);
      if (sample < 1024)
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
    float deltaLuzAmbiente = 960 - 160; //luz maxima menos luz minima ambiente
    int minimoLed = 0;
    int maximoLed = 160;
    float deltaPotenciaLed = maximoLed - minimoLed; // delta valores del led
    float porcentajeLuzAmbiente = (lectura_FotoResistencia - deltaPotenciaLed)/deltaLuzAmbiente;
    float porcentajeIluminacionLED = 1-porcentajeLuzAmbiente;
    int valorLed = minimoLed + (porcentajeIluminacionLED * deltaPotenciaLed);
    return valorLed;
  }
