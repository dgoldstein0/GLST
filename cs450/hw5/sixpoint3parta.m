%Yongzuan Wu wu68 cs450 HW5 6.3 (a)
format long;
f=@(x) x.^4-14*x.^3+60*x.^2-70*x;
min=fminbnd(f,0,3);
disp('the minimun on interval [0,3] is ');
fprintf('%14.12f\n',min);
x=0:0.01:3;
y=f(x);
plot(x,y);
disp('the function is unimodel');
