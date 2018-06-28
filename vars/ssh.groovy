



def call(args){
// 	String key = 
// """-----BEGIN RSA PRIVATE KEY-----
// MIIEogIBAAKCAQEAuBay0u0qY/RSEQYLOw7FrBCtgp2nQTZrXA7XvMeZCdCyQxWU
// RSk3axJNYjLNomIWob0IcbmASXq2Hexdx5Yv/AI8jdzmrwRX+7aYWx+EGdIHBEWK
// AXyeqTJ+yxwmag24TaQJp1LFlQFUYdlGVjb2u512JzH5VEpSTukFWS4uMaKE+YJP
// LxRym0BkNJsyiDmHNZePdp/I2tiRiMarKwpP1ldqDt7ocvvkAeEAvOXATNJ1RR9f
// kbDKq8IJ2bOMKcZOqYDal3kPIuy4QF66EtQq9EFmGNpAA2OomvOyC96I2EO8X+8E
// 5CmemaR7Ixc5HY5yZg3I1O8y+RI2VwIKwwVxewIDAQABAoIBAEQqw5UvlV7l/e2n
// 20jHM5N/qYCQeHrBeAfixyh0kMr9qMUGnJzvokHXySfvwA29XLwPj6ztEAAlnIKA
// IEnj52jeOyYdAp8UcLAY8Vns1bHqAAco6O2cA/SLakAz8FWdNv4pHBoFoHyIvYJu
// Pol2WO8oY7Qn4TfBPbFSwFRBsYFXGC9t05fIu4YKXJs6eS/NjMJMNvYJ7FLEJwaL
// fLRXrnaMVIsJJ84v1v0XoKtBJ3Ybj08LW6jbXa97r/DLfCzjPvPLlRQAY6pk+uK3
// 9JwcwZl1vYU7FNpML+sbWZ3z/vnRrNWMa/3fH3O0K83Bm2CbzztOOnh95IuO4WNz
// kq253SECgYEA3Zvt3t1JzFErl8HPLI+xomBgOwESIAzgh4SHTvHfV8HWIKsY21Wf
// 2aUgCRUHxZH1CDeph1QETpre+vC4OvvfvRcGRpBiaY5BAPvRNhBYqhlC7DWmg6bQ
// MzX9a1/TqEbapgzg+kpGr35uYztEqrCeyCRSm6duPxDXHH5TW7iptQ8CgYEA1Kgp
// BPUK4N6htCCGheIuOPjnOsfTQcxReYLD53hfY3GvdUecgDsccIH8G11qQDEFysiR
// tRb9pFUlDtzgrng2h+CZHllP3g0XAWiOyeJ73XN+2HVcY+fEl8qIxWnc6qIyiusy
// 0dLxsD9VqSoN9KROJHbXAbra0XXYT7SYVwHJdNUCgYB6+5fswH2+B6XbmFDy9+Oq
// qzV8SUHXy42nZ2L36r75orbEdm9a+Y+2Zy76G1OuzytenhVU5sllHgl01bZH9ZPb
// CjYPXv1eevWUKCuYh0XNUCuVjsiT8zVD1kiHGC0MDBY3ZJpODH5h0hZMt42J9G6J
// v66gdzB1i1D1nR2Bn19+/QKBgH8xwa8sXgBYSUHy/mNQALOk9LEs2kgNzGGwfDqT
// vgI9+mNcnNf6Ay2sIb3AZw/UApHi23wEL1u+bbbxGBZi4sLs7MjBRMsc6zi1cEqC
// g/HV5fXUZJmBBA6CXsItaqyQrUh4G2JzGIqBp1vSQpykNfULe9iWCdGk+efarg/E
// itmtAoGACiTLUBGT2/hI3JNlPKG7dQ9xJPMbH9CvRIbvB4yNPi7NwhTZhCxgxIl0
// c+xlsTW5wtOQ4yVKIDYlFFa2ngNikE9psT1y1ax3ZOibvlI80MicrnWTIqdmxF6+
// bE3PgvaKq9kTx3sD00D+SNN438RJhFckhUALjQvncNDZJA/fZzY=
// -----END RSA PRIVATE KEY-----

// """
// 	String keyPath = "/var/jenkins_home/workspace/petclinic_pipeline_master-SB4IZJUDDU473F33XRGM7HJYLNLXKCH3DORA7ALO7SGM6FBSUCKQ/tmpkey.txt"
// 	sh "touch tmpkey.txt"
// 	sh "echo '$key' >> $keyPath" 
// 	sh "chmod 0600 $keyPath"
// 	sh "ssh -i $keyPath $args"
// 	sh "rm $keyPath"
	String roleID = "d2ad2ecf-7105-168b-6b15-5e4c56d63f10"
	sh """ 
		export SECRET_ID= $(cat ~/secret.txt) 
		touch ~/payload.json
		echo {'role_id': '$roleID', 'secret_id': '$SECRET_ID'} > ~/payload.json 
		cat ~/payload.json 
		cd ~/; curl -o output.txt --request POST --data @payload.json http://127.0.0.1:8200/v1/auth/approle/login 
	"""

}

