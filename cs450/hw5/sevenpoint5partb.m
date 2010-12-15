%Yongzuan Wu wu68 cs450 HW5 7.5 (b) (c) (d)
format long;
x=[0 0.5 1.0 6.0 7.0 9.0]';
y=[0 1.6 2.0 2.0 1.5 0]';
p=spline(x,y);
t=0:0.01:9;
f=ppval(p,t);
plot(x,y,'o',t,f,'-')
disp('(c)');
disp('The cubic spline give more reasonable resullt.');
disp('The single 5-degree interpolation will results in a curve with more ');
disp('oscillation due to the nature of a relatively high degree polynomial');
disp('The cubic spline is more smooth between data points');
disp('(d)');
disp('No. Piecewise linear interpolation is not smooth at the data points');