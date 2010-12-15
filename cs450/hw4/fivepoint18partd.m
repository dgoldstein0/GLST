%Yongzuan Wu wu68 cs450 HW4 5.18 (a)
%define f(x) and the jacobian Jx
format long;
f=@(x)[(x(1,1)+3)*(x(2,1)*x(2,1)*x(2,1)-7)+18; sin(x(2,1)*exp(x(1,1))-1)];
Jx=@(x) [x(2,1)*x(2,1)*x(2,1)-7 3*(x(1,1)+3)*x(2,1)*x(2,1); ...
    x(2,1)*exp(x(1,1))*cos(x(2,1)*exp(x(1,1))-1) ...
    cos(x(2,1)*exp(x(1,1))-1)*exp(x(1,1))];

x=[-0.5 1.4]';
xnext=x;
B=Jx(x);
Bnext=B;
done=0;        %flag for finishing
while (done==0)
   x=xnext;
   B=Bnext;
   s=B\(-f(x));
   xnext=x+s;
   y=f(xnext)-f(x);
   Bnext=B+((y-B*s)*s'/(s'*s));
   if (xnext==x)
       done=1;
   end;
end;
disp('The solution x* is');
fprintf('%14.12f\n',x);


