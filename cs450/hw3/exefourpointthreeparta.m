%Yongzuan Wu wu68 cs450 HW3 4.3 (a)
X0=[1; 0; 0];
A=[6 2 1; 2 3 1; 1 1 1];
shift=eye(3,3);
shift=2*shift;       %compute shift
A=A-shift;
disp(' k                    Xk                                          |Eigenvalue|   ');
fprintf('%2d  %15d  %15d    %15d  \n',0, 0, 0, 1);
X=A\X0;  
X=X/max(abs(X));
eigen=0;
k=1;
done=0;
while (done==0)    
    Y=A\X;
    norm=max(abs(Y));                 %normalize  
    if (abs(norm-eigen)>1e-6)    %compute with 6 digit precision
      X=Y/norm;
      eigen=norm;
      fprintf('%2d  %15d  %15d    %15d        %12.6f \n', k , X(1,1), X(2,1), X(3,1),1/eigen);        
    else
        done=1;
    end; 
    k=k+1;
end;
eigen=1/eigen;
%determine the sign of eigenvalue
xx=A*X;
if (xx(3,1)/X(3,1)<0)
    eigen=-eigen;
end;    
eigen=eigen+2;          %restore shift
fprintf('eigenvalue equals %10.6f \n',eigen);

disp('the eigenvalue of A closest to 2 is 2.133074, and a corresponding eigenvector is [0.6069200 -1 -0.3469145]\n')
