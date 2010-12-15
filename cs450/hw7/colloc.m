function F=colloc(x)
n=size(x,1);
F=zeros(n,1);
F(1,1)=x(1,1);
for i=1:n
    F(n,1)=F(n,1)+x(i,1);
end
F(n,1)=F(n,1)-1;
for i=2:n-1
   t=(i-1)./(n-1);
   u=0;
   for j=1:n
       u=u+x(j,1)*t.^(j-1);
   end
   for j=3:n
       F(i,1)=F(i,1)+(j-1)*(j-2)*x(j,1)*t.^(j-3);
   end
   F(i,1)=F(i,1)-(10*u.^3+3*u+t.^2);
end


