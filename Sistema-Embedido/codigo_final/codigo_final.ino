#include<stdlib.h>
const int sampleWindow = 50; // Ancho ventana en mS (50 mS = 20Hz)
unsigned int signalMin;
const int BUZZERPin = 44; //pin del buzzer
int LEDPin = 12; //pin del led
int i;//Variable auxiliar para condicionales
int MICPin = A8;
byte PIRPin = 34; // Input for HC-S501
int pir_value; // Place to store read PIR Value
const int LDRPin = A3;
int LDR_value;
int encenderLed=0;
int flagMic=0;
int flagbluetooth=0;
unsigned long ledMillis;     
int intensidad_luz;
int contador = 0;
char c = ' ';
char read_string[10];
int regulando=0;
unsigned long inicio; //se prendio el led
unsigned long fin;    //se apago el led
unsigned long Tiempo_Parcial; //lo que duro prendido una vez
unsigned long Tiempo_Total = 0; //lo que lleva prendido desde que inicio arduino



void setup() {
  pinMode(BUZZERPin, OUTPUT);  //definir pin como salida del buzzer (pwm)
  noTone(BUZZERPin);
  pinMode(LEDPin, OUTPUT);//Se inicializa como OUTPUT el pin a usar. Debe ser PWM
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
   unsigned int umbral=mediaSonido()*40+signalMin;
   unsigned long stopMillis= millis()+1000;   
   int lectura_Infrarrojo;       
   int Tiempo_Alarna = 20000; //Si el celu no esta conectado la alarma durara este tiempo, luego puede configurarse por bt.

   while (millis() < stopMillis)
   {                 
    
    // Actualizar la intenzidad de la luz
    if(encenderLed==1)
    {
      regulando = 1;
      Encender_Led();
      regulando = 0;         
    }

    // Verificamos el estado del sensor de sonido para encender/apagar led
    if( Verificar_Sensor_Sonido(umbral,encenderLed) == 1 )
    {
    
      encenderLed = 1;
      flagMic = 1;
    }//El flag del mic es para evitar que el sensor de movimiento me apague el led si lo prendemos por aplauso
    else
    {
      
      encenderLed = 0;
      flagMic=0;
    }
    

    // Activacióm del la luz por sensor de Bluetooth
    if( Verificar_Bluetooth() == 1 ) //Verificar_Bluetooth() == 1 )Verificar_Serial() == 1)
    {
    
      encenderLed = 1;
      flagMic = 1; //El flag del mic es para evitar que el sensor de movimiento me apague el led si lo prendemos por bluetooth
    
    }
    else
    {
      
      encenderLed = 0;
      flagMic = 0;
            
    }    
/*
    if( Verificar_Sensor_Infrarrojo(encenderLed) == 1 )
    {
      
      encenderLed=1;      
    }
    else
    {
      encenderLed=0;       
    }   */     
  }
}

int Verificar_Sensor_Infrarrojo(int ultimo_estado)
{
  // Activación del la luz por sensor de Bluetooth 
  if( digitalRead(PIRPin) == 1 && flagMic==0)
  {                   
      Encender_Led();                     
      encenderLed = 1;
      return 1;     
  }
  else if( ultimo_estado == 0 ) 
  {       
     Apagar_Led();         
     encenderLed = 0;
     return 0;    
  }
  return encenderLed;
  
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
      Encender_Led();
      encenderLed = 1;
      return 1;    
    }
    if( c == '2')
    {      
       Apagar_Led(); 
       encenderLed = 0;
       return 0;             
    }
    if( c == '3')
    {
      while (c == '3')
      {        
        analogWrite(LEDPin,HIGH);       
        delay(100);
        analogWrite(LEDPin,LOW);
        delay(100); 
        if(Serial.available()){
          aux = Serial.read();             
          if( aux == '3' )
            c = 0;
        }        
      }
      Encender_Led();
      encenderLed = 1;
    }
    if(c=='4')
    {
      ledMillis=millis();
    }
    if(c=='5')
    {
      //
    }
        
  }
  return encenderLed;  
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
             
      Encender_Led();
      encenderLed = 1;
      return 1;
    
    }
    if( c == '2')
    {
      
       Apagar_Led();       
       encenderLed = 0;
       return 0;             
    }
    if( c == '3')
    {
      
      while (c == '3')
      {
        tone(BUZZERPin, 500); //activa un tono de frecuencia determinada en un pin dado        
        Encender_Led();                              
        noTone(BUZZERPin);           //detiene el tono en el pin
        delay(25);        
        Apagar_Led();
        delay(25); 
        if(Serial3.available()){
          aux = Serial3.read();             
          if( aux == '3' )
            c = '0';
        }             
      }
      Encender_Led();
      encenderLed = 1;      
    }
    if( c == '4')
    {
      Serial.println("entre");
      Serial.println(contador);
      contador++;
      if( encenderLed == 0 )
      {
        Encender_Led(); 
        Serial.println("encender por agitacion");             
        encenderLed = 1;
      }
      else if ( encenderLed == 1 )
      {
        analogWrite(LEDPin,LOW);       
        Serial.println("apagar por agitacion");
        encenderLed = 0;
      }
    }
    /*
    if( c == '5' )
    {
      String b;
      Serial.println("detecte 5");
      while( Serial3.available())
      {       
          b = Serial3.read();
          int resultado = atoi(b[0]);
          intensidad_luz = resultado;        
          Serial.println(intensidad_luz);          
        }  
     }*/             
 }

  return encenderLed;  
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
    
      Encender_Led();
      delay(100);
      return 1;
      
    }
    else
    {
      
      Apagar_Led();     
      delay(100);
      return 0;      

    }      
  }

  return encenderLed;
    
}

int Encender_Led()
{
  analogWrite(LEDPin,regularIntensidadLuminica(analogRead(LDRPin)));
  if(regulando == 0)
  {
      inicio = millis();         
  }
}

int Apagar_Led()
{
  analogWrite(LEDPin,LOW);
  fin = millis();
  Tiempo_Parcial = (fin - inicio);
  Tiempo_Total += Tiempo_Parcial;
  Serial.print(inicio);
  Serial.print("\t");
  Serial.print(fin);
  Serial.print("\t");
  Serial.print(Tiempo_Parcial);
  Serial.print("\t");
  Serial.println(Tiempo_Total);      
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
    int maximoLed = 250;
    float deltaPotenciaLed = maximoLed - minimoLed; // delta valores del led
    float porcentajeLuzAmbiente = (lectura_FotoResistencia - deltaPotenciaLed)/deltaLuzAmbiente;
    float porcentajeIluminacionLED = 1-porcentajeLuzAmbiente;
    int valorLed = minimoLed + (porcentajeIluminacionLED * deltaPotenciaLed);
    return valorLed;
  }
