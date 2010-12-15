%Yongzuan Wu wu68 cs450 HW6 8.4
format long;
a=@(x) sqrt(x.^3);
b=@(x) 1./(1+10*x.^2);
c=@(x) (exp(-9*x.^2)+exp(-1024*(x-1/4).^2))/sqrt(pi);
d=@(x) 50./(pi*(2500*x.^2+1));
e=@(x) 1./sqrt(abs(x));
f=@(x) 25*exp(-25*x);
g=@(x) log(x);

%conjecture is correct is relative error is <1%
disp('the numerical approximation to part (a) is ');
val=quadl(a,0,1,1e-8)
if (abs(val-0.4)/0.4<1e-2)
    disp('the conjecture is correct');
else disp('the conjecture is incorrect'); disp(''); end;
    
disp('the numerical approximation to part (b) is ');
val=quadl(b,0,1,1e-8)
if (abs(val-0.4)/0.4<1e-2)
    disp('the conjecture is correct');
else disp('the conjecture is incorrect'); disp(''); end;

disp('the numerical approximation to part (c) is ');
val=quadl(c,0,1,1e-8)
if (abs(val-0.2)/0.2<1e-2)
    disp('the conjecture is correct');
else disp('the conjecture is incorrect'); disp(''); end;
    
disp('the numerical approximation to part (d) is ');
val=quadl(d,0,10,1e-8)
if (abs(val-0.5)/0.5<1e-2)
    disp('the conjecture is correct');
else disp('the conjecture is incorrect'); disp(''); end;

disp('the numerical approximation to part (e) is ');
val=quadl(e,-9,100,1e-8)
if (abs(val-26)/26<1e-2)
    disp('the conjecture is correct');
else disp('the conjecture is incorrect'); disp(''); end;

disp('the numerical approximation to part (f) is ');
val=quadl(f,0,10,1e-8)
if (abs(val-1)<1e-2)
    disp('the conjecture is correct');
else disp('the conjecture is incorrect'); disp(''); end;

disp('the numerical approximation to part (g) is ');
val=quadl(g,0,1,1e-8)
if (abs(val+1)<1e-2)
    disp('the conjecture is correct');
else disp('the conjecture is incorrect'); disp(''); end;
    
    
    
    
    
