%Yongzuan Wu wu68 cs450 HW5 7.5
format long;
x=[0 0.5 1.0 6.0 7.0 9.0]';
y=[0 1.6 2.0 2.0 1.5 0]';
p=polyfit(x,y,5);
t=0:0.01:9;
f=polyval(p,t);
plot(x,y,'o',t,f,'-');