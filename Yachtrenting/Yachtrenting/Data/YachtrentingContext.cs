using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;

namespace Yachtrenting.Data
{
    public class YachtrentingContext : DbContext
    {
        public YachtrentingContext (DbContextOptions<YachtrentingContext> options)
            : base(options)
        {
        }

        public DbSet<Berles> Berles { get; set; } = default!;
    }
}
