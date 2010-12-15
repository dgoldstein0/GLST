%Yongzuan Wu wu68 cs450 HW4 5.18 (a)
%define f(x) and the jacobian Jx
format long;
f=@(x)[(x(1,1)+3)*(x(2,1)*x(2,1)*x(2,1)-7)+18; sin(x(2,1)*exp(x(1,1))-1)];
Jx=@(x) [x(2,1)*x(2,1)*x(2,1)-7 3*(x(1,1)+3)*x(2,1)*x(2,1); ...
    x(2,1)*exp(x(1,1))*cos(x(2,1)*exp(x(1,1))-1) ...
    cos(x(2,1)*exp(x(1,1))-1)*exp(x(1,1))];
%run two scheme at the same time
bx=[-0.5 1.4]';
bxnext=bx;
x=[-0.5 1.4]';
xnext=x;
B=Jx(bx);
Bnext=B;
k=0;
done1=0; done2=0;       %flag for finishing
solution=[0 1]';
disp('k       Broyden''s x1   Broyden''s x2   Err of BM x1    Err of BM x2     Newton''s x1      Newton''s x2     Err of NM x1    Err of NM x2 ');
while (done1==0)||(done2==0)
   k=k+1;      %counter
   if (done1==0)
      %run of Broyden 
       bx=bxnext;
      B=Bnext;
      s=B\(-f(bx));
      bxnext=bx+s;
       ErrBM=bxnext-solution;
       y=f(bxnext)-f(bx);
      Bnext=B+((y-B*s)*s'/(s'*s)); 
      if (bxnext==bx)  
       done1=1;     %Broyden finish
       end;
   end;
   %run of Newton
   if (done2==0)
   x=xnext;
   s=Jx(x)\(-f(x));
   xnext=x+s;
   ErrNM=solution-xnext;
   if (xnext==x)
       done2=1;
   end;
   end;
   fprintf('%2d   %14.12f  %14.12f  %14.12f  %14.12f  %14.12f  %14.12f  %14.12f  %14.12f\n',...
       k, bxnext(1,1), bxnext(2,1), ErrBM(1,1), ErrBM(2,1),xnext(1,1), xnext(2,1), ErrNM(1,1), ErrNM(2,1));
end;
disp('Newton''s method converges in 3 iteratiopns, whereas Broyden''s method converges in 7 iterations.')


