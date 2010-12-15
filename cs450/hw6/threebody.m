function dy = threebody(t,y)

G=6.67259e-11;
M=5.974e24;
m=7.348e22;
miustar=M/(m+M);
miu=m/(m+M);
D=3.844e8;
d=4.669e6;
r1=@(x,y) sqrt((x+d).^2+y.^2);
r2=@(x,y) sqrt((D-d-x).^2+y.^2);
omega=2.661e-6;

dy = zeros(4,1);    % a column vector
dy(1) = y(2);
dy(2) = -G*(M*(y(1)+miu*D)./(r1(y(1),y(3)).^3)+...
    m*(y(1)-miustar*D)./(r2(y(1),y(3)).^3))+omega^2*y(1)+2*omega*y(4);
dy(3) = y(4);
dy(4) = -G*(M*y(3)./(r1(y(1),y(3)).^3)+m*y(3)./(r2(y(1),y(3)).^3))+...
    omega^2*y(3)-2*omega*y(2);

