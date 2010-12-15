function F=finitdif(y)
n=size(y,1);
F=zeros(n,1);
F(1,1)=y(1,1); 
F(n,1)=y(n,1)-1;
h=1./(n-1); 
for i=2:n-1
   F(i,1)=(y(i+1,1)-2*y(i,1)+y(i-1,1))./(h.^2)-(10*y(i,1).^3+3*y(i,1)+((i-1)./(n-1)).^2);   
end


