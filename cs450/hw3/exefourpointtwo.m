%Yongzuan Wu wu68 cs450 HW3 4.2 part a
X0=[0; 0; 1];
A=[2 3 2; 10 3 4; 3 6 1];
disp(' k                    Xk                                          |Eigenvalue|   ');
fprintf('%2d  %15d  %15d    %15d  \n',0, 0, 0, 1);
X=A*X0;            
eigen=0;
k=1;
done=0;
while (done==0)    
    Y=A*X;
    norm=max(abs(Y));                 %normalize  
    if (abs(norm-eigen)>1e-6)    %compute with 6 digit precision
      X=Y/norm;
      eigen=norm;
      fprintf('%2d  %15d  %15d    %15d        %10.6f \n', k , X(1,1), X(2,1), X(3,1),eigen);        
    else
        done=1;
    end; 
    k=k+1;
end;
%determine the sign of eigenvalue
xx=A*X;
if (xx(3,1)/X(3,1)<0)
    eigen=-eigen;
end;
fprintf('eigenvalue equals %10.6f \n',eigen);
disp('the dominant eigenvalue of A is 11.000001, and a corresponding eigenvector is [0.5 1 0.75]\n')

 