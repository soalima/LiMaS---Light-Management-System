/**PRUEBA DE BUZZER**/

/*const int pin = 1;
 
void setup() {
  pinMode(pin, OUTPUT);  //definir pin como salida
}
 
void loop(){
  digitalWrite(pin, HIGH);   // poner el Pin en HIGH
  delay(500);               // esperar 5 segundos
  digitalWrite(pin, LOW);    // poner el Pin en LOW
  delay(200);               // esperar 20 segundos
}*/



/**PRUEBA DE LED**/

/*------Declaración de variables-----*/
/*int led = 12; //Se escoje la variable donde se conectará el LED
int i;//Variable auxiliar para condicionales
 
void setup() {                
  pinMode(led, OUTPUT);//Se inicializa como OUTPUT el pin a usar. Debe ser PWM
}
 
void loop() {
//Se recorren los valores desde el 0 al 255 para enviar una señal PWM con ciclo de trabajo de 0% a 100%
//aumentando en 2.55% el ciclo de trabajo cada 10 mili segundos.  
  for(i=0; i<256; i++){ 
    analogWrite(led,i);
    delay(10);
  }
//Se recorren los valores del 255 al 0 de mayor a menor igual que el for anterior.
  for(i=255; i>-1; i--){
    analogWrite(led,i);
    delay(10);
  }
}*/


/**PRUEBA DE MICROFONO**/
/*
const int pinMicrophone = A2;

void setup ()
{
  Serial.begin(9600);
}

void loop ()
{
  int valorSonido = analogRead(pinMicrophone);
  if(valorSonido > 90)
    {
      Serial.println("SONIDO FUERTE LCOO BAJEN EL VLOUEM");
      Serial.println(valorSonido);  
    }
}*/

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
}
