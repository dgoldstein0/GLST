%Yongzuan Wu wu68 cs450 HW5 6.3 (b
format long;
%X(1,1)=x,    X(2,1)=y
f=@(x) 2*x(1,1)^2+1.05*x(1,1)^4+x(1,1)^6/6+x(1,1)*x(2,1)+x(2,1)^2;
%define gradient
disp('the second component of gradient=0 implies x+2y=0, thus we can plug in this equation into')
disp('the first componemt and get a polynomial of x');
%poly=@(x) 4*x-4*1.05*x.^3+x.^5-x/2;
poly=[1 0 -4.20 0 3.5 0];
%solve poly(x)=0
r=roots(poly)
disp('there are five critical points');
%compute the x,y vector
x=r;
y=-x/2;
%define the Hessian matrix
H=@(x) [5*x.^4-12*1.05*x.^2+4 1; 1 2];
%check the eigenvalues of H and find the minimum
min=10000000;
minx=1:1:2;
for k=1:1:5
    xi=x(k,1);
    yi=-xi/2;
    xvec=[xi yi]';
    if (f(xvec)<min)
        min=f(xvec);
        minx=xvec;
    end;
    disp('when x=');
    fprintf('%14.12f',xi);
    Hm=H(xi);
    disp(', the eigen values are ');
    eigen=eig(Hm)
    if (eigen>0)
        disp('x=');
        fprintf('%14.12f',xi);
        disp(' is a minimum');
    elseif (eigen<0)
        disp('x=');
        fprintf('%14.12f',xi);
        disp(' is a maximum');
    else
        disp('x=');
        fprintf('%14.12f',xi);
        disp(' is a saddle point');
    end;
end;
disp('the global minimum occurs at x=');
fprintf('%12.10f',minx');
disp('the global minimum is');
fprintf('%14.12f',min);
