#include "INIT.h"
#include "LeVel.h"
#include "TemPer.h"
#include "ComBLE.h"
#include <SoftwareSerial.h>

/* global Variable */
int L[4] = {0,0,0,0};
int wLevel,nLevel;
int wTemp,nTemp;
float WARM,COOL;
int pW, pC, pB;
int ONOF;
int state, state_chk, state_per;
int send_chk, bt_chk;
int reg;
int value,LB,LC,LW;
int rece_chk = 0;

/* var */
uint8_t buf[10];
BOX* box; 

void setup() {
  initlevel();
  inittemp();
  initbt();
  initONOF();
  Serial.begin(9600);
  pW = OFF_bath;
  pC = OFF_bath;
  pB = OFF_bath;
  box = (BOX*)malloc(sizeof(BOX));
  memset(box,'\0',sizeof(BOX));
  state_chk = ST_CHK;
  send_chk = SEN_CHK;
  pump_set(OFF_bath, OFF_bath);
}


void loop() {
  
    value = digitalRead(BUTTON);
    
///////////////////////////////////////////////예기치 않게(블루투스때문에) 종료//////////////
    if( box->my_exe == 0 ){
      state = BATH_OFF;
      pW = OFF_bath;
      pC = OFF_bath;
      pump_set(pC,pW);   
    }

    if( value == HIGH ){
      if( ONOF == 1 ){
        exiting_bath();   
      }
      pB = ON_bath;
      delay(10);
      Serial.println("< take of the water >------------------------------------------------------------------------------------");
    }
    else {
      pB = OFF_bath;
    }
    digitalWrite(pumpB,pB);

    set_LED();
    
  ///////////////////////////////////////////////////////////////////////////////////////
  /*  if(Serial.available()>0){
      test_readData();     
      initialbox_my(box);
      ONOF = box->request_exe;
      wLevel = box->request_level;
      wTemp = box->request_temp + 15;
      rece_chk = 1;
    }
*/
    /* Communicate with server with BLE */
  
  if(readData(buf)){
    
//      if( buf[0] < 128 && bt_chk == 0 ){
//        break;        
//      }
      bt_chk == 1;
      rece_chk = 1;
      analysisCommand(buf,box);
      initialbox_my(box);
      ///////////////////////////
      ONOF = box->request_exe;
      if(!ONOF){
        Serial.println("Forced stop");
        box -> my_exe = 0;
        sendData(box);
      }
      else{
        Serial.println("Start");
        box -> my_exe = 1; 
        wLevel = box->request_level;     wTemp = box->request_temp + 15;
        ///////////////////////////
        getallTemp();
        sendData(box);
      }
   }
    
//   if(bt_chk == 0 ){
//    continue;
//   }

/////////////////////////////////////////////////////////////////////////////////////////
    getallTemp();
    if( ONOF == 0 ){
      Serial.println("nothing");
      box->my_exe = 0;
      state = BATH_OFF;           // Nosending
      pW = OFF_bath;
      pC = OFF_bath;
      pump_set(pC,pW);
    }
    /* machine acting*/
    if( ONOF==1 ){
      Serial.println("pumping");
      box->my_exe = ONOF;
      
      /* chk water level*/
      L[1] = analogRead(L1);
      L[2] = analogRead(L2);
      L[3] = analogRead(L3);
      nLevel = chk_level(L);
      box -> my_level = nLevel;
      if( nLevel == wLevel ){
           exiting_bath();
      }
      
      set_LED();
      /* chk water temperature */
      
      
      /* define state */
      if( (state_chk--) == 0 ){
        define_state();
        state_chk = ST_CHK;
      }
  
      state_per = state_chk*100/ST_CHK;
      
      /* do motor with state */
      motor_state();
      pump_set(pC,pW);
      if((send_chk--) == 0){
        sendData(box);
        send_chk =SEN_CHK;
      }
   }
   
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  if((send_chk--) == 0 ){
//    if(ONOF == 0 ){
//      Serial.println("< BATH OFF >--------------------------------------------------------------------------------------------");
//      if(rece_chk == 1 ){
//        sendData(box);
//        rece_chk = 0;
//      }
//      print_pump_stat();
//      print_temp(nTemp, WARM, COOL);
//    }
//    if(ONOF == 1 ){
//      Serial.println("< BATH ONN >--------------------------------------------------------------------------------------------");
//      sendData(box);
//      rece_chk = 0;
//      print_pump_stat();
//      print_temp(nTemp, WARM, COOL);
//      print_box(box);
//    }
//    send_chk = SEN_CHK;
//    Serial.println();
//    
//  }  
  
  delay(20);
}

void set_LED(){
  if( ONOF == 0 ){
    LB = LED_OFF;
    LC = LED_OFF;
    LW = LED_OFF;
  }
  if(ONOF == 1 ){
    LB = LED_ON;
  }
  digitalWrite(LedONOF,LB);
  digitalWrite(LedC,LC);
  digitalWrite(LedW,LW);
}

void getallTemp(){
  nTemp = (int)getTemp_Bath();
  WARM = getTemp_Warm();
  COOL = getTemp_Cool();
  reg = nTemp - 19;
  if( reg <= 0 )  reg = 0;
  if( reg >= 30 ) reg = 30;
  box -> my_temp = reg;
  
}

void define_state(){
  int temp;

  if( ONOF == 0 ){
    state == BATH_OFF;
    return;
  }
  temp = nTemp-wTemp;          //temp > 0 make cool, temp < 0 make hot
  Serial.print("compare temp  ");
  Serial.print(temp);
  if( temp > 2 ){
    Serial.println("\t\tmaking cool");
    LC = LED_ON;
    LW = LED_OFF;
  }
  else if (temp <-2 ) {
    LC = LED_OFF;
    LW = LED_ON;
    Serial.println("\t\tmaking warm");
  }
  else {
    LC = LED_ON; LW = LED_ON;
    Serial.println("\t\ITS ENOUGH");
  }
  if( temp > tmpH ) {
    state = BATH_W3;
    return;
  }
  if( temp > tmpM ) {
    state = BATH_W2;
    return;
  }
  if( temp > tmpL ) {
    state = BATH_W2;
    return;
  }
  temp = temp * (-1);
  if( temp > tmpH ) {
    state = BATH_C3;
    return;
  }
  if( temp > tmpM ) {
    state = BATH_C2;
    return;
  }
  if( temp > tmpL ) {
    state = BATH_C1;
    return;
  }
  state = (int)wTemp;
  return;  
}

void motor_state(){
    if( state == BATH_OFF ){
      pW = OFF_bath;      pC = OFF_bath;
      return;
    }
    
    if( state == BATH_W1 ){       //make cool
      pW = OFF_bath;      pC = ON_bath;
//      if( state_per >= 80 )
    if( state_per >= 90 )
        pW = ON_bath;
      return;
    }
    
    if( state == BATH_W2 ){
      pW = OFF_bath;      pC = ON_bath;
//      if( state_per >= 60 )
      if( state_per >= 70 )
        pW = ON_bath;
      return;
    }
    
    if( state == BATH_W3 ){
      pW = OFF_bath;      pC = ON_bath;
      return;
    }
    
    if( state == BATH_EE ){
      pW = ON_bath;       pC = ON_bath;
      return;
    }
    
    if( state == BATH_C1 ){       //make hot
      pW = ON_bath;       pC = OFF_bath;
 //     if( state_per >= 80 )
      if(state_per >=60 )
        pC = ON_bath;
      return;
    }
    
    if( state == BATH_C2){
      pW = ON_bath;       pC = OFF_bath;
//      if( state_per >= 60 )
     if( state_per >= 40 )
        pC = ON_bath;
      return;
    } 
    if( state == BATH_C3){
      pW = ON_bath;       pC = OFF_bath;
      return;
    } 
}
void exiting_bath(){
    ONOF = 0;
    box->my_exe = 0;
    state = BATH_OFF;
    sendData(box);
    rece_chk = 0;
    pW = OFF_bath;
    pC = OFF_bath;
    pump_set(pC,pW);   
}
//////////////////////////////////////////////////////////////////////////////////////////
void print_pump_stat(){
      Serial.print("\tSTATE : ");
      if (state == BATH_OFF ) Serial.println("BATH_OFF");
      if (state == BATH_W3 ) Serial.println("COOLING LEVEL 3333");
      if (state == BATH_W2 ) Serial.println("COOLING LEVEL 2222");
      if (state == BATH_W1 ) Serial.println("COOLING LEVEL 1111");
      if (state == BATH_EE ) Serial.println("ENOUGHH LEVEL 0000");
      if (state == BATH_C1 ) Serial.println("WARMING LEVEL 1111");
      if (state == BATH_C2 ) Serial.println("WARMING LEVEL 2222");
      if (state == BATH_C3 ) Serial.println("WARMING LEVEL 3333");
      Serial.print("\tWARM BATH ::::");
      if( pW == ON_bath ) Serial.print("ONN\tCOOL BATH ::::");
      else                Serial.print("OFF\tCOOL BATH ::::");
      if( pC == ON_bath ) Serial.println("ONN");
      else                Serial.println("OFF");
  
}

void test_readData(){
   int data[4];   int i=0;   int a;
   while(Serial.available()>0 ) {
    a = Serial.read();
    if( i==0 ){
      data[0] = a-48;
      i++;
      continue;
    }
    if( i==1 ){
      data[1] = a-48;
      i++;
      continue;
    }
    if( i==2 ){
      if( a == 49 ) data[2] = 10;
      if( a == 50 ) data[2] = 15;
      if( a == 51 ) data[2] = 20;
      if( a == 52 ) data[2] = 25;
      if( a == 53 ) data[2] = 30;
      i++;
      continue;
    }
    if( i==3 ){
      data[3] = a-48;
      i++;
      continue;
    }    
  }
  box -> request_exe = data[0]; 
  box -> request_level = data[1]; 
  box -> request_temp = data[2];
  box -> my_exe = data[4];
  Serial.print("!RECEIVE data! : exe - ");
  Serial.print(data[0]);
  Serial.print("\tlevel - ");
  Serial.print(data[1]);
  Serial.print("\ttemp - ");
  Serial.print(data[2]);
  Serial.print("( ");
  Serial.print(data[2]+15);
  Serial.print(" )");
  Serial.print("\t\tmy exe - ");
  Serial.println(data[3]);
  Serial.println();
}
